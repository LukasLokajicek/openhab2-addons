package org.openhab.binding.forcomfort.util;

public class BytesUtil {

    public static String byteHexString(byte... bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        final char[] hexChars = new char[bytes.length * 3 - 1];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if (j < (bytes.length - 1)) {
                hexChars[j * 3 + 2] = ' ';
            }
        }
        return new String(hexChars);
    }

}
