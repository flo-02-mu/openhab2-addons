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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.worxlandroid.internal.mqtt.MowerInfo;
import org.openhab.binding.worxlandroid.internal.mqtt.MqttConnection;
import org.openhab.binding.worxlandroid.internal.restconnection.Mower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
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

    @Nullable
    private ScheduledFuture<?> refreshJob;
    private final int DEFAULT_REFRESH_INTERVAL = 10;

    public WorxLandroidHandler(Thing thing) {
        super(thing);
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
        if (CHANNEL_1.equals(channelUID.getId())) {
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
                startMqttConnection();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Worx server states mower as offline");
            }
        }
    };

    @Override
    public void connectionLost(@Nullable Throwable throwable) {
        logger.warn("MQTT connection lost, automatically reconnecting");
    }

    @Override
    public void messageArrived(@Nullable String s, @Nullable MqttMessage mqttMessage) {
        logger.debug("On topic {} received message {}",s,mqttMessage);
        mower = new Gson().fromJson(mqttMessage.getPayload().toString(), MowerInfo.class);
        logger.debug("Parsed object serial number: {}",mower.getCfg().getSn());
        logger.debug("Parsed object RSI: {}",mower.getDat().getRsi());
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
