package org.openhab.binding.forcomfort.physical;

import java.util.Arrays;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.forcomfort.util.BytesUtil;

public class ByteArrayType implements Command, State {

    final byte[] bytes;

    public ByteArrayType(byte... bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public String format(String pattern) {
        return String.format(pattern, bytes);
    }

    @Override
    public String toFullString() {
        return BytesUtil.byteHexString(bytes);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

}
