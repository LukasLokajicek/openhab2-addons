package org.openhab.binding.forcomfort.elements;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.types.State;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.forcomfort.ForComfortBindingConstants;
import org.openhab.binding.forcomfort.internal.AbstractElement;
import org.openhab.binding.forcomfort.internal.AbstractElement.ElementParam;
import org.openhab.binding.forcomfort.internal.AbstractElement.ElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutterElement extends AbstractElement {

    private static final ElementType TYPE = ElementType.ShutterElement;
    private static final Logger logger = LoggerFactory.getLogger(ShutterElement.class);
    public static final String JSON_SHUTTER_PERCENTAGE = "percentage";
    public static final String JSON_SHUTTER_DIRECTION = "direction";

    private final int onOffPosition;
    private final int statePosition;

    private enum ShutterCommand implements State {
        OPEN,
        CLOSE,
        STOP;

        @Override
        public String format(String pattern) {
            return String.format(pattern, name());
        }

        @Override
        public String toFullString() {
            return name();
        }
    };

    /**
     * 100 % - Open
     * 0 % - Close
     */
    public PercentType state = new PercentType(0);

    public ShutterElement(ThingListener listener, int moduleAddress, int onOffPosition, int statePosition) {
        super(listener, moduleAddress, onOffPosition);
        this.onOffPosition = onOffPosition;
        this.statePosition = statePosition;
    }

    @Override
    public String commandToJson(OnOffType onOffType) {
        boolean isOn = isOnCommand(onOffType);
        JSONObject json = createHeader();
        try {
            json.put(JSON_SHUTTER_DIRECTION, isOn ? ShutterCommand.OPEN : ShutterCommand.CLOSE);
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
        }
        return json.toString();
    }

    @Override
    public ElementType getType() {
        return TYPE;
    }

    @Override
    public String commandToJson(PercentType cmd) {
        JSONObject json = createHeader();
        try {
            json.put(JSON_SHUTTER_PERCENTAGE, cmd.intValue());
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
        }
        return json.toString();
    }

    @Override
    public String commandToJson(IncreaseDecreaseType cmd) {
        boolean isOn = IncreaseDecreaseType.INCREASE == cmd;
        JSONObject json = createHeader();
        try {
            json.put(JSON_SHUTTER_DIRECTION, isOn ? ShutterCommand.CLOSE : ShutterCommand.CLOSE);
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
        }
        return json.toString();
    }

    @Override
    public String commandToJson(StopMoveType cmd) {
        JSONObject json = createHeader();
        ShutterCommand shutterCommand = StopMoveType.STOP == cmd ? ShutterCommand.STOP : ShutterCommand.OPEN;
        try {
            json.put(JSON_SHUTTER_DIRECTION, shutterCommand);
        } catch (JSONException e) {
            logger.warn("Cannot create Json object", e);
        }
        return json.toString();
    }

    @Override
    public String commandToJson(HSBType cmd) {
        logger.debug("HSBType is not suported by ShutterElement!");
        return null;
    }

    @Override
    public void stateUpdate(ElementParam param, Object o) {
        Integer percent;
        if (o instanceof Integer) {
            percent = (Integer) o;
        } else {
            logger.debug("ShutterElement supports only Integer parameter.");
            return;
        }
        final State state = new PercentType(percent);
        listener.updateStateThing(ForComfortBindingConstants.CHANNEL_SHUTTER, state);
    }

    public int getOnOffPosition() {
        return onOffPosition;
    }

    public int getStatePosition() {
        return statePosition;
    }

}
