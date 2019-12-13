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
package org.openhab.binding.broadlink.internal.handler;

import com.github.mob41.blapi.FloureonDevice;
import com.github.mob41.blapi.dev.hysen.BaseStatusInfo;
import com.github.mob41.blapi.dev.hysen.SensorControl;
import com.github.mob41.blapi.mac.Mac;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

/**
 * The {@link FloureonThermostatHandler} is responsible for handling thermostats labeled as Floureon Thermostat.
 *
 * @author Florian Mueller - Initial contribution
 */
public class FloureonThermostatHandler extends BroadlinkHandler {

    private final Logger logger = LoggerFactory.getLogger(FloureonThermostatHandler.class);
    private FloureonDevice floureonDevice;

    /**
     * Creates a new instance of this class for the {@link FloureonThermostatHandler}.
     *
     * @param thing the thing that should be handled, not null
     */
    public FloureonThermostatHandler(Thing thing) {
        super(thing);
        try {
            blDevice = new FloureonDevice(host, new Mac(mac));
            this.floureonDevice = (FloureonDevice) blDevice;
        } catch (IOException e) {
            logger.error("Could not find broadlink device at Host {} with MAC {} ", host, mac, e);
            updateStatus(ThingStatus.OFFLINE);
        }


    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command class: {}", command.getClass());
        logger.debug("Command: {}", command.toFullString());

        if(command == RefreshType.REFRESH){
            refreshData();
            return;
        }

        switch (channelUID.getIdWithoutGroup()) {
            case SETPOINT:
                handleSetpointCommand(channelUID, command);
                break;
            case POWER:
                handlePowerCommand(channelUID, command);
                break;
            case MODE:
                handleModeCommand(channelUID, command);
                break;
            case SENSOR:
                handleSensorCommand(channelUID,command);
                break;
            case REMOTE_LOCK:
                handleRemoteLockCommand(channelUID,command);
                break;
            default:
                logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handlePowerCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            try {
                floureonDevice.setPower(command == OnOffType.ON);
            } catch (Exception e) {
                logger.error("Error while setting power of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleModeCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                if(MODE_AUTO.equals(command.toFullString())){
                    floureonDevice.switchToAuto();
                }else{
                    floureonDevice.switchToManual();
                }
            } catch (Exception e) {
                logger.error("Error while setting power of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleSetpointCommand(ChannelUID channelUID, Command command) {
        if (command instanceof QuantityType) {
            try {
                floureonDevice.setThermostatTemp(((QuantityType) command).doubleValue());
            } catch (Exception e) {
                logger.error("Error while setting setpoint of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleSensorCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                BaseStatusInfo statusInfo = floureonDevice.getBasicStatus();
                if(SENSOR_INTERNAL.equals(command.toFullString())){
                    floureonDevice.setMode(statusInfo.getAutoMode(),statusInfo.getLoopMode(), SensorControl.INTERNAL);
                }else{
                    floureonDevice.setMode(statusInfo.getAutoMode(),statusInfo.getLoopMode(), SensorControl.EXTERNAL);
                }
            } catch (Exception e) {
                logger.error("Error while trying to set sensor mode {}: ",command,e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleRemoteLockCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            try {
                floureonDevice.setLock(command == OnOffType.ON);
            } catch (Exception e) {
                logger.error("Error while setting remote lock of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    @Override
    protected void refreshData() {
        try {
            BaseStatusInfo baseStatusInfo = floureonDevice.getBasicStatus();
            if(baseStatusInfo == null){
                logger.warn("Device {} did not return any data. Trying to reauthenticate...",thing.getUID());
                authenticate();
                baseStatusInfo = floureonDevice.getBasicStatus();
            }
            if(baseStatusInfo == null){
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,"Device not responding.");
                return;
            }
            logger.debug("Retrieved data from device {}: {}", thing.getUID(), baseStatusInfo);
            logger.debug("Updating channel {} with value {}", ROOM_TEMPERATURE, new DecimalType(baseStatusInfo.getRoomTemp()));
            logger.debug("Mode {}",StringType.valueOf(baseStatusInfo.getAutoMode() ? "auto" : "manual"));
            updateState(ROOM_TEMPERATURE, new DecimalType(baseStatusInfo.getRoomTemp()));
            logger.debug("Updating channel {} with value {}", ROOM_TEMPERATURE_EXTERNAL_SENSOR, new DecimalType(baseStatusInfo.getExternalTemp()));
            updateState(ROOM_TEMPERATURE_EXTERNAL_SENSOR, new DecimalType(baseStatusInfo.getExternalTemp()));
            updateState(SETPOINT, new DecimalType(baseStatusInfo.getThermostatTemp()));
            updateState(POWER, OnOffType.from(baseStatusInfo.getPower()));
            updateState(MODE, StringType.valueOf(baseStatusInfo.getAutoMode() ? "auto" : "manual"));
            updateState(SENSOR,StringType.valueOf(baseStatusInfo.getSensorControl().name()));
            logger.debug("Updating channel {} with value {}", SENSOR, new StringType(baseStatusInfo.getSensorControl().name()));
            updateState(TEMPERATURE_OFFSET, new DecimalType(baseStatusInfo.getDif()));
            updateState(ACTIVE, OnOffType.from(baseStatusInfo.getActive()));
            updateState(REMOTE_LOCK, OnOffType.from(baseStatusInfo.getRemoteLock()));
        } catch (Exception e) {
            logger.error("Error while retrieving data for {}", thing.getUID(), e);
        }

    }
}
