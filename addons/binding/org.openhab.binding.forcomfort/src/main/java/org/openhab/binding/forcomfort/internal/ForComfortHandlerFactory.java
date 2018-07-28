/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.forcomfort.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.forcomfort.handler.ForcomfortBridgeHandler;
import org.openhab.binding.forcomfort.handler.ForcomfortThingHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link ForComfortHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Lukas_L - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.forcomfort", service = ThingHandlerFactory.class)
public class ForComfortHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(ForComfortHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(ForcomfortBridgeHandler.SUPPORTED_THING_TYPES, ForcomfortThingHandler.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (ForcomfortBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            ForcomfortBridgeHandler handler = new ForcomfortBridgeHandler((Bridge) thing);
            return handler;
        } else if (ForcomfortThingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new ForcomfortThingHandler(thing);
        }
        logger.warn("Thing Type {} is not supported", thing.getThingTypeUID());
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        thingHandler.dispose();
    }

}
