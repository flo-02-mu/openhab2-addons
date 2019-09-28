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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.semantics.model.property.Properties;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.worxlandroid.internal.WorxLandroidConfiguration;
import org.openhab.binding.worxlandroid.internal.mqtt.MqttConnection;
import org.openhab.binding.worxlandroid.internal.restconnection.Mower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.CHANNEL_1;
import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.TOPIC_COMMAND_IN;
import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.TOPIC_COMMAND_OUT;

/**
 * The {@link WorxLandroidHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public class WorxLandroidHandler extends BaseThingHandler implements MqttCallback {

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidHandler.class);

    private @Nullable WorxLandroidConfiguration config;
    private @NonNullByDefault({}) MqttConnection mqttConnection;
    private @NonNullByDefault({}) Mower mower;

    public WorxLandroidHandler(Thing thing) {
        super(thing);
        initialize();

        /*
            if (refreshJob == null || refreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} min.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThings, INITIAL_DELAY_IN_SECONDS,
                        TimeUnit.MINUTES.toSeconds(refreshInterval), TimeUnit.SECONDS);
            }

         */

    }

    @Override
    public void initialize(){
        Bridge bridge2 = this.getBridge();
        if(bridge2 == null){
            logger.warn("No bridge selected yet, cannot initialize thing");
            //updateStatus(ThingStatus.UNINITIALIZED);
            return;
        }

        logger.debug("Bridge: {}",bridge2);
        logger.debug("Bridge.getHandler: {}",bridge2.getHandler());
        WorxLandroidAPIHandler bridge = (WorxLandroidAPIHandler)getBridge().getHandler();
        Map<String, String> properties = thing.getProperties();
        try {
            mqttConnection = new MqttConnection(bridge.getKeyStore(),
                    properties.get(TOPIC_COMMAND_IN),
                    properties.get(TOPIC_COMMAND_OUT),
                    bridge.getUserInfo().getMqtt_endpoint());

            mqttConnection.start(this);
        } catch (MqttException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            logger.error("Error while establishing MQTT connection to worx server: {}",e);
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

    @Override
    public void dispose(){
        logger.debug("Disposing thing {}",thing);
        if(mqttConnection != null) {
            try {
                mqttConnection.stop();
            } catch (MqttException e) {
                logger.error("Error while disconnecting from mqtt server: {}", e);
            }
        }
    }

    @Override
    public void connectionLost(@Nullable Throwable throwable) {
        logger.warn("MQTT connection lost, automatically reconnecting");
    }

    @Override
    public void messageArrived(@Nullable String s, @Nullable MqttMessage mqttMessage) {
        logger.debug("On topic {} received message {}",s,mqttMessage);
        mower = new Gson().fromJson(mqttMessage.getPayload().toString(),Mower.class);
        logger.debug("Parsed object serial number: {}",mower.getSerial_number());
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
