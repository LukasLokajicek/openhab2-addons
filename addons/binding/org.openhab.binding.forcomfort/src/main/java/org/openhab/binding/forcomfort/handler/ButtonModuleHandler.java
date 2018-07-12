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
package org.openhab.binding.forcomfort.handler;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.forcomfort.internal.ForComfortBindingConstants;
import org.openhab.binding.forcomfort.physical.ByteArrayListener;
import org.openhab.binding.forcomfort.physical.ThingRegistrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ButtonModuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author LukasLokajicek - Initial contribution
 */
@NonNullByDefault
public class ButtonModuleHandler extends BaseThingHandler implements ByteArrayListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(ForComfortBindingConstants.THING_TYPE_BUTTON_MODULE);

    private final Logger logger = LoggerFactory.getLogger(ButtonModuleHandler.class);

    public ButtonModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("{} is read-only handler. Received command {} from channelUID {}", this.getClass().getSimpleName(),
                command, channelUID);
    }

    @Override
    public void initialize() {
        String addressString = (String) getConfig().get(ForComfortBindingConstants.MODULE_ADDRESS);
        final int addressNumber;
        try {
            addressNumber = Integer.parseInt(addressString, 16);
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The address cannot be parsed. Please, provide the number in hex format");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "There is no bridge for the " + getClass().getSimpleName());
            return;
        }
        boolean registered = ThingRegistrator.register(bridge, thing, addressNumber);
        if (registered) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Bridge already contains thing with the address: " + Integer.toHexString(addressNumber));
        }
    }

    @Override
    public void onDataReceived(byte... data) {
        // TODO Auto-generated method stub

    }
}
