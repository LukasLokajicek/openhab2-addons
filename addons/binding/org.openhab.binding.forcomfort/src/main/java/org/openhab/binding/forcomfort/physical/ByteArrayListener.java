package org.openhab.binding.forcomfort.physical;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNull;

public interface ByteArrayListener extends EventListener {

    void onDataReceived(byte @NonNull... data);

}
