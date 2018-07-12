package org.openhab.binding.forcomfort.handler;

import static org.openhab.binding.forcomfort.internal.ForComfortBindingConstants.THING_TYPE_WIRE_BRIDGE;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.forcomfort.internal.ForComfortBindingConstants;
import org.openhab.binding.forcomfort.physical.ByteArrayListener;
import org.openhab.binding.forcomfort.physical.ByteArrayType;
import org.openhab.binding.forcomfort.util.BytesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForComfortBridgeHandler extends BaseBridgeHandler implements ByteArrayListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WIRE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(ForComfortBridgeHandler.class);

    private Map<Integer, Thing> things = new HashMap<>();

    private final SerialHandler serialHandler;

    private String portName;

    public ForComfortBridgeHandler(final Bridge bridge) {
        super(bridge);
        serialHandler = new SerialHandler(this);
    }

    @Override
    public void initialize() {
        portName = (String) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_PORT);
        int baud = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_BAUD_RATE)).intValue();
        int dataBits = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_DATA_BITS)).intValue();
        int parity = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_PARITY)).intValue();
        int stopBits = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_STOP_BITS)).intValue();
        int bufferSize = ((BigDecimal) getConfig().get(ForComfortBindingConstants.BRIDGE_CONFIG_BUFFER_SIZE))
                .intValue();
        int sleep = 0;
        serialHandler.setParametres(portName, baud, dataBits, parity, stopBits, bufferSize, sleep);
        UpdateStatusHolder status = serialHandler.initialize();
        if (status == null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(status.getThingStatus(), status.getThingStatusDetail(), status.getDescription());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof ByteArrayType) {
            ByteArrayType bytes = (ByteArrayType) command;
            UpdateStatusHolder status = serialHandler.write(bytes.getBytes());
            if (status != null) {
                updateStatus(status.getThingStatus(), status.getThingStatusDetail(), status.getDescription());
            }
        }
    }

    @Override
    public void onDataReceived(byte... bytes) {
        logger.debug("Data from {}: {}", portName, BytesUtil.byteHexString(bytes));
    }

    public boolean containsKey(Object key) {
        return things.containsKey(key);
    }

    public Thing get(Integer key) {
        return things.get(key);
    }

    public Thing put(Integer key, Thing value) {
        return things.put(key, value);
    }

    public Thing remove(Integer key) {
        return things.remove(key);
    }

    public void clear() {
        things.clear();
    }

}
