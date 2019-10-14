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
package org.openhab.binding.worxlandroid.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WorxLandroidBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public class WorxLandroidBindingConstants {



    private WorxLandroidBindingConstants() {
    }
    private static final String BINDING_ID = "worxlandroid";

    // Config parameter
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // Thing parameter
    public static final String MAC = "mac";
    public static final String NAME = "name";
    public static final String FIRMWARE_VERSION = "firmware_version";
    public static final String TOPIC_COMMAND_IN = "topic_command_in";
    public static final String TOPIC_COMMAND_OUT = "topic_command_out";
    public static final String SERIAL_NUMBER = "serial_number";


    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WORX_LANDROID_API = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_MOWER = new ThingTypeUID(BINDING_ID, "mower");

    // List of all Channel ids

    public static final String BATTERY_GROUP = "battery";
    public static final String SCHEDULE_GROUP = "mowerSchedule";
    public static final String STATISTICS = "statistics";
    public static final String OTHER = "other";
    public static final String LOCKED = "locked";
    public static final String DISTANCE_COVERED = "distanceCovered";
    public static final String MOWER_WORKING_TIME = "mowerWorkTime";
    public static final String BLADE_WORKING_TIME = "bladeWorkTime";
    public static final String BATTERY_TEMPERATURE = "batteryTemperature";
    public static final String BATTERY_CHARGE_CYCLES = "batteryChargeCycles";
    public static final String BATTERY_VOLTAGE = "batteryVoltage";
    public static final String BATTERY_PERCENTAGE = "batteryPercentage";
    public static final String BATTERY_CHARGING = "batteryCharging";
    public static final String BATTERY_STATE = "batteryState";
    public static final String MESSAGES_IN = "messagesIn";
    public static final String MESSAGES_OUT = "messagesOut";
    public static final String PUSH_NOTIFICATIONS = "pushNotifications";
    public static final String RSI = "rsi";
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS_DESCRIPTION = "statusDescription";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_DESCRIPTION = "errorDescription";
    public static final String LAST_UPDATE = "lastUpdate";
    public static final String MOWER_COMMAND = "mowerCommand";
    public static final String START_TIME_MONDAY = "startTimeMonday";
    public static final String DURATION_MONDAY = "durationMonday";
    public static final String CUT_EDGE_MONDAY = "cutEdgeMonday";
    public static final String START_TIME_TUESDAY = "startTimeTuesday";
    public static final String DURATION_TUESDAY = "durationTuesday";
    public static final String CUT_EDGE_TUESDAY = "cutEdgeTuesday";
    public static final String START_TIME_WEDNESDAY = "startTimeWednesday";
    public static final String DURATION_WEDNESDAY = "durationWednesday";
    public static final String CUT_EDGE_WEDNESDAY = "cutEdgeWednesday";
    public static final String START_TIME_THURSDAY = "startTimeThursday";
    public static final String DURATION_THURSDAY = "durationThursday";
    public static final String CUT_EDGE_THURSDAY = "cutEdgeThursday";
    public static final String START_TIME_FRIDAY = "startTimeFriday";
    public static final String DURATION_FRIDAY = "durationFriday";
    public static final String CUT_EDGE_FRIDAY = "cutEdgeFriday";
    public static final String START_TIME_SATURDAY = "startTimeSaturday";
    public static final String DURATION_SATURDAY = "durationSaturday";
    public static final String CUT_EDGE_SATURDAY = "cutEdgeSaturday";
    public static final String START_TIME_SUNDAY = "startTimeSunday";
    public static final String DURATION_SUNDAY = "durationSunday";
    public static final String CUT_EDGE_SUNDAY = "cutEdgeSunday";

}
