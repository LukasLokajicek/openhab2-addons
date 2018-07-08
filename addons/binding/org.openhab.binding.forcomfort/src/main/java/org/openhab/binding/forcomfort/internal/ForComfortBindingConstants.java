/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.forcomfort.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ForComfortBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author LukasLokajicek - Initial contribution
 */
@NonNullByDefault
public class ForComfortBindingConstants {

    private static final String BINDING_ID = "forcomfort";

    // List of all Thing Type UIDs

    // 4com4t wire bridge
    public final static ThingTypeUID THING_TYPE_WIRE_BRIDGE = new ThingTypeUID(BINDING_ID, "wireBridge");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    // Bridge configuration (COM port configuration)
    public static final String BRIDGE_CONFIG_PORT = "port";
    public static final String BRIDGE_CONFIG_BAUD_RATE = "baudRate";
    public static final String BRIDGE_CONFIG_DATA_BITS = "dataBits";
    public static final String BRIDGE_CONFIG_PARITY = "parity";
    public static final String BRIDGE_CONFIG_STOP_BITS = "stopBits";
    public static final String BRIDGE_CONFIG_BUFFER_SIZE = "bufferSize";
}
