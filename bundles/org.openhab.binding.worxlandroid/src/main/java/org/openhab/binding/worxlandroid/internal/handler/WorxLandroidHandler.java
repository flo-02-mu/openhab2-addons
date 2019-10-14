/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.worxlandroid.internal.handler;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.worxlandroid.internal.WorxLandroidException;
import org.openhab.binding.worxlandroid.internal.mqtt.ErrorCodeEnum;
import org.openhab.binding.worxlandroid.internal.mqtt.MowerCommandEnum;
import org.openhab.binding.worxlandroid.internal.mqtt.MowerInfo;
import org.openhab.binding.worxlandroid.internal.mqtt.MowerInfoDeserializer;
import org.openhab.binding.worxlandroid.internal.mqtt.MqttConnection;
import org.openhab.binding.worxlandroid.internal.mqtt.StatusCodeEnum;
import org.openhab.binding.worxlandroid.internal.restconnection.Mower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.*;

/**
 * The {@link WorxLandroidHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public class WorxLandroidHandler extends BaseThingHandler implements MqttCallback {

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidHandler.class);

    private @Nullable Configuration config;
    private @Nullable WorxLandroidAPIHandler bridge;
    private @NonNullByDefault({})
    MqttConnection mqttConnection;
    private @NonNullByDefault({})
    MowerInfo mowerInfo;
    private final Gson gson;

    @Nullable
    private ScheduledFuture<?> refreshJob;
    private static final int DEFAULT_REFRESH_INTERVAL = 10;

    public WorxLandroidHandler(Thing thing) {
        super(thing);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(MowerInfo.class, new MowerInfoDeserializer())
                .create();
        initialize();
    }

    @Override
    public void initialize() {

        if (this.getBridge() == null) {
            logger.warn("No bridge selected yet, cannot initialize thing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "No bridge selected yet!");
            return;
        }

        this.bridge = (WorxLandroidAPIHandler) getBridge().getHandler();
        config = thing.getConfiguration();

        // Only if the mowerInfo device is online (connected to wifi), the device can be
        // controlled/checked via the MQTT queue.
        // Therefore the status is checked via REST every 10 minutes.
        if (refreshJob == null || refreshJob.isCancelled()) {
            logger.debug("Start refresh job at interval {} min.", DEFAULT_REFRESH_INTERVAL);
            refreshJob = scheduler.scheduleWithFixedDelay(statusJob, 0,
                    DEFAULT_REFRESH_INTERVAL, TimeUnit.MINUTES);
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((OTHER+"#"+MOWER_COMMAND).equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                logger.error("Channel {} does not support any commands", channelUID);
                return;
            }
            try {
                String message = "{\"cmd\":" + MowerCommandEnum.valueOf(command.toString()).id + "}";
                logger.debug("Sending command {} to works server",message);
                mqttConnection.sendMessage(message);
            } catch (MqttException e) {
                logger.error("Error while updating channel {}", channelUID);
            }
        }
    }

    private void startMqttConnection() {
        logger.debug("Starting MQTT connection...");
        try {
            mqttConnection = new MqttConnection(bridge.getKeyStore(),
                    (String) config.get(TOPIC_COMMAND_IN),
                    (String) config.get(TOPIC_COMMAND_OUT),
                    bridge.getUserInfo().getMqtt_endpoint());

            mqttConnection.start(this);
        } catch (MqttException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            logger.error("Error while establishing MQTT connection to worx server: {}", e);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", thing);
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        if (mqttConnection != null) {
            try {
                mqttConnection.stop();
            } catch (MqttException e) {
                logger.error("Error while disconnecting from mqtt server: ", e);
            }
        }
    }

    private Runnable statusJob = () -> {
        String serialNumber = (String) config.get(SERIAL_NUMBER);
        Mower mower = null;
        try {
            mower = bridge.getWorxLandroidRESTConnection().getMowerStatus(serialNumber);
        } catch (WorxLandroidException e) {
            updateStatus(ThingStatus.OFFLINE,ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        logger.debug("Reply for status call on serial {} : {}", serialNumber, mower.isOnline());
        if (mower.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
            if (mqttConnection != null) {
                logger.debug("Is connected: {}", mqttConnection.isConnected());
            }
            if (mqttConnection == null || !mqttConnection.isConnected()) {
                startMqttConnection();
            } else {
                logger.debug("MQTT client still connected, not opening a new one.");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Worx server states mowerInfo as offline");
            try {
                mqttConnection.stop();
            } catch (MqttException e) {
                logger.error("Error while closing mqtt cocnnection: ", e);
            }
        }
    };

    @Override
    public void connectionLost(@Nullable Throwable throwable) {
        logger.warn("MQTT connection lost, automatically reconnecting {}", throwable);
    }

    @Override
    public void messageArrived(@Nullable String s, @Nullable MqttMessage mqttMessage) {
        if (mqttMessage == null) {
            logger.warn("Empty message received on topic {}", s);
            return;
        }
        String message = mqttMessage.toString();
        logger.debug("On topic {} received message {}", s, message);
        try {

            mowerInfo = gson.fromJson(message, MowerInfo.class);
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.error("Error while serializing object:", e);
        }

        logger.debug("Parsed object serial number: {}", mowerInfo.getConfiguration().getSerialNumber());
        logger.debug("Parsed object RSI: {}", mowerInfo.getData().getRsi());

        updateState(OTHER+"#"+RSI, new DecimalType(mowerInfo.getData().getRsi()));
        logger.debug("Parsed object Battery temperature: {}", mowerInfo.getData().getBattery().getTemperature());
        updateState(STATISTICS+"#"+DISTANCE_COVERED, new QuantityType<>(mowerInfo.getData().getStatistic().getDistance(), SIUnits.METRE));
        updateState(STATISTICS+"#"+MOWER_WORKING_TIME, new QuantityType<>(mowerInfo.getData().getStatistic().getWorkingTime().toHours(), SmartHomeUnits.HOUR));
        updateState(STATISTICS+"#"+BLADE_WORKING_TIME, new QuantityType<>(mowerInfo.getData().getStatistic().getBladeWorkingTime().toHours(), SmartHomeUnits.HOUR));
        updateState(BATTERY_GROUP+"#"+BATTERY_TEMPERATURE, new QuantityType<>(mowerInfo.getData().getBattery().getTemperature(), SIUnits.CELSIUS));
        updateState(BATTERY_GROUP+"#"+BATTERY_CHARGE_CYCLES, new DecimalType(mowerInfo.getData().getBattery().getChargeCycle()));
        updateState(BATTERY_GROUP+"#"+BATTERY_VOLTAGE, new QuantityType<>(mowerInfo.getData().getBattery().getVoltage(), SmartHomeUnits.VOLT));
        updateState(BATTERY_GROUP+"#"+BATTERY_PERCENTAGE, new DecimalType(mowerInfo.getData().getBattery().getPercentage()));
        updateState(BATTERY_GROUP+"#"+BATTERY_CHARGING, OnOffType.from(mowerInfo.getData().getBattery().isCharging()));
        updateState(BATTERY_GROUP+"#"+BATTERY_STATE, new DecimalType(mowerInfo.getData().getBattery().getState()));
        updateState(OTHER+"#"+STATUS_CODE, new DecimalType(mowerInfo.getData().getStatus()));
        updateState(OTHER+"#"+STATUS_DESCRIPTION, new StringType(StatusCodeEnum.getById(mowerInfo.getData().getStatus()).toString()));
        updateState(OTHER+"#"+ERROR_CODE, new DecimalType(mowerInfo.getData().getError()));
        updateState(OTHER+"#"+ERROR_DESCRIPTION, new StringType(ErrorCodeEnum.getById(mowerInfo.getData().getError()).toString()));
        ZonedDateTime zonedDateTime = mowerInfo.getConfiguration().getDateTime().atZone(ZoneId.systemDefault());
        updateState(OTHER+"#"+LAST_UPDATE, new DateTimeType(zonedDateTime));

        // Monday
        MowerInfo.MowerStart mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.MONDAY.getValue()+1);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_MONDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Monday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_MONDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_MONDAY,OnOffType.from(mowerStart.isCutEdge()));

        // Tuesday
        mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.TUESDAY.getValue()+1);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_TUESDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Tuesday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_TUESDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_TUESDAY,OnOffType.from(mowerStart.isCutEdge()));

        // Wednesday
        mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.WEDNESDAY.getValue()+1);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_WEDNESDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Wednesday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_WEDNESDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_WEDNESDAY,OnOffType.from(mowerStart.isCutEdge()));

        // Thursday
        mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.THURSDAY.getValue()+1);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_THURSDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Thursday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_THURSDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_THURSDAY,OnOffType.from(mowerStart.isCutEdge()));

        // Friday
        mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.FRIDAY.getValue()+1);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_FRIDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Friday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_FRIDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_FRIDAY,OnOffType.from(mowerStart.isCutEdge()));

        // Saturday
        mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.SATURDAY.getValue()+1);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_SATURDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Saturday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_SATURDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_SATURDAY,OnOffType.from(mowerStart.isCutEdge()));

        // Sunday
        mowerStart = mowerInfo.getConfiguration().getSchedule().getMowerStarts().get(DayOfWeek.SUNDAY.getValue()-6);
        updateState(SCHEDULE_GROUP+"#"+ START_TIME_SUNDAY,new StringType(mowerStart.getTimeOfDay()));
        logger.debug("Mower duration Sunday: {} - in minutes: ",mowerStart.getDuration(),mowerStart.getDuration().toMinutes());
        updateState(SCHEDULE_GROUP+"#"+ DURATION_SUNDAY,new QuantityType<>(mowerStart.getDuration().toMinutes(),SmartHomeUnits.MINUTE));
        updateState(SCHEDULE_GROUP+"#"+ CUT_EDGE_SUNDAY,OnOffType.from(mowerStart.isCutEdge()));
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken iMqttDeliveryToken) {
        if (iMqttDeliveryToken != null ) {
            logger.debug("Message nr {} with content delivered successfully.", iMqttDeliveryToken.getMessageId());
        }
    }
}
