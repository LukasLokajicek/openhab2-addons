package org.openhab.binding.forcomfort.physical;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.openhab.binding.forcomfort.handler.ForComfortBridgeHandler;

public class ThingRegistrator {

    @NonNullByDefault
    public static boolean register(Bridge bridge, Thing thing, Integer address) {
        BridgeHandler handler = bridge.getHandler();
        if (handler instanceof ForComfortBridgeHandler) {
            ForComfortBridgeHandler forComfortBridgeHandler = (ForComfortBridgeHandler) handler;
            if (forComfortBridgeHandler.containsKey(address)) {
                return false;
            }
            forComfortBridgeHandler.put(address, thing);
            return true;
        }
        return false;
    }

    @NonNullByDefault
    public static boolean unregister(Bridge bridge, Integer address) {
        BridgeHandler handler = bridge.getHandler();
        if (handler instanceof ForComfortBridgeHandler) {
            ForComfortBridgeHandler forComfortBridgeHandler = (ForComfortBridgeHandler) handler;
            return forComfortBridgeHandler.remove(address) != null;
        }
        return false;
    }

}
