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
    public static final String CHANNEL_1 = "channel1";

}
