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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.worxlandroid.internal.mqtt.MowerInfo;
import org.openhab.binding.worxlandroid.internal.mqtt.MowerInfoDeserializer;
import org.openhab.binding.worxlandroid.internal.mqtt.MqttConnection;
import org.openhab.binding.worxlandroid.internal.restconnection.Mower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Time;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.time.temporal.Temporal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private @NonNullByDefault({}) MqttConnection mqttConnection;
    private @NonNullByDefault({}) MowerInfo mower;
    private final Gson gson;

    @Nullable
    private ScheduledFuture<?> refreshJob;
    private final int DEFAULT_REFRESH_INTERVAL = 10;

    public WorxLandroidHandler(Thing thing) {
        super(thing);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(MowerInfo.class, new MowerInfoDeserializer())
                .create();
        initialize();

    }

    @Override
    public void initialize(){

        if(this.getBridge() == null){
            logger.warn("No bridge selected yet, cannot initialize thing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "No bridge selected yet!");
            return;
        }

        this.bridge = (WorxLandroidAPIHandler)getBridge().getHandler();
        config = thing.getConfiguration();

        // Only if the mower device is online (connected to wifi), the device can be
        // controlled/checked via the MQTT queue.
        // Therefore the status is checked via REST every 10 minutes.
        if (refreshJob == null || refreshJob.isCancelled()) {
            logger.debug("Start refresh job at interval {} min.", DEFAULT_REFRESH_INTERVAL);
            refreshJob = scheduler.scheduleWithFixedDelay(statusJob,0,
                    DEFAULT_REFRESH_INTERVAL, TimeUnit.MINUTES);
        }

    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RSI.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    private void startMqttConnection(){
        logger.debug("Starting MQTT connection...");
        try {
            mqttConnection = new MqttConnection(bridge.getKeyStore(),
                    (String)config.get(TOPIC_COMMAND_IN),
                    (String)config.get(TOPIC_COMMAND_OUT),
                    bridge.getUserInfo().getMqtt_endpoint());

            mqttConnection.start(this);
        } catch (MqttException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            logger.error("Error while establishing MQTT connection to worx server: {}",e);
        }
    }

    @Override
    public void dispose(){
        logger.debug("Disposing thing {}",thing);
        if(refreshJob != null) {
            refreshJob.cancel(true);
        }
        if(mqttConnection != null) {
            try {
                mqttConnection.stop();
            } catch (MqttException e) {
                logger.error("Error while disconnecting from mqtt server: ", e);
            }
        }
    }

    private Runnable statusJob = new Runnable() {
        @Override
        public void run() {
            Mower mower = bridge.getWorxLandroidRESTConnection().getMowerStatus((String) config.get(SERIAL_NUMBER));
            logger.debug("Reply for status call on serial {} : {}", (String) config.get(SERIAL_NUMBER), mower);
            if (mower.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
                if(mqttConnection != null){
                    logger.debug("Is connected? "+mqttConnection.isConnected());
                }
                if(mqttConnection == null || !mqttConnection.isConnected()){
                    startMqttConnection();
                }else{
                    logger.debug("MQTT client still connected, not opening a new one.");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Worx server states mower as offline");
                try {
                    mqttConnection.stop();
                } catch (MqttException e) {
                    logger.error("Error while closing mqtt cocnnection: ",e);
                }
            }
        }
    };

    @Override
    public void connectionLost(@Nullable Throwable throwable) {
        logger.warn("MQTT connection lost, automatically reconnecting {}",throwable);
    }

    @Override
    public void messageArrived(@Nullable String s, @Nullable MqttMessage mqttMessage) {
        logger.debug("On topic {} received message {}",s,mqttMessage.toString());
        try {

            mower = gson.fromJson(mqttMessage.toString(), MowerInfo.class);
        }catch(JsonSyntaxException |IllegalStateException e){
            logger.error("Error while serializing object:", e);
        }

        logger.debug("Parsed object serial number: {}",mower.getConfiguration().getSerialNumber());

        logger.debug("Parsed object RSI: {}",mower.getData().getRsi());
        updateState(RSI, new DecimalType(mower.getData().getRsi()));
        logger.debug("Parsed object Battery temperature: {}",mower.getData().getBattery().getTemperature());
        updateState(BATTERY_TEMPERATURE, new QuantityType<>(mower.getData().getBattery().getTemperature(),SIUnits.CELSIUS));
        updateState(DISTANCE_COVERED, new QuantityType<>(mower.getData().getStatistic().getDistance(),SIUnits.METRE));
        updateState(MOWER_WORKING_TIME, new QuantityType<>(mower.getData().getStatistic().getWorkingTime().toHours(), SmartHomeUnits.HOUR));
        updateState(BLADE_WORKING_TIME, new QuantityType<>(mower.getData().getStatistic().getBladeWorkingTime().toHours(), SmartHomeUnits.HOUR));
        updateState(BATTERY_CHARGE_CYCLES, new DecimalType(mower.getData().getBattery().getChargeCycle()));
        updateState(BATTERY_VOLTAGE, new QuantityType<>(mower.getData().getBattery().getVoltage(),SmartHomeUnits.VOLT));
        updateState(BATTERY_PERCENTAGE,new DecimalType(mower.getData().getBattery().getPercentage()));
        updateState(BATTERY_CHARGING, OnOffType.from(mower.getData().getBattery().isCharging()));
        updateState(BATTERY_STATE, new DecimalType(mower.getData().getBattery().getState()));
        updateState(STATUS_CODE, new DecimalType(mower.getData().getStatus()));
        updateState(ERROR_CODE,new DecimalType(mower.getData().getError()));
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
