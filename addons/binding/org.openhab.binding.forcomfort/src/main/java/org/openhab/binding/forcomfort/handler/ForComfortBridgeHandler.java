package org.openhab.binding.forcomfort.handler;

import static org.openhab.binding.forcomfort.internal.ForComfortBindingConstants.THING_TYPE_WIRE_BRIDGE;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.forcomfort.internal.ForComfortBindingConstants;
import org.openhab.binding.forcomfort.physical.ByteArrayType;
import org.openhab.binding.forcomfort.util.BytesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForComfortBridgeHandler extends SerialBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WIRE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(ForComfortBridgeHandler.class);

    public ForComfortBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        portName = (String) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_PORT);
        baud = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_BAUD_RATE)).intValue();
        dataBits = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_DATA_BITS)).intValue();
        parity = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_PARITY)).intValue();
        stopBits = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_STOP_BITS)).intValue();
        bufferSize = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_BUFFER_SIZE)).intValue();
        sleep = 0;
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof ByteArrayType) {
            ByteArrayType bytes = (ByteArrayType) command;
            write(bytes.getBytes());
        }
    }

    @Override
    public void onDataReceived(byte... bytes) {
        logger.debug("Data from {}: {}", portName, BytesUtil.byteHexString(bytes));
    }
}
