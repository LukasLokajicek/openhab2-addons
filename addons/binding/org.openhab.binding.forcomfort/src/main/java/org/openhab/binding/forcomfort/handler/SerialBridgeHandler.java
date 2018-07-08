package org.openhab.binding.forcomfort.handler;

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.forcomfort.util.BytesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link SerialBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels. Thing Handler classes that use serial
 * communications can extend/implement this class, but must make sure they
 * supplement the configuration parameters into the {@link SerialConfiguration}
 * Configuration of the underlying Thing, if not already specified in the
 * thing.xml definition
 *
 * @author Karel Goderis - Initial contribution
 */
public abstract class SerialBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {

    // List of all Configuration parameters
    public static final String PORT = "port";
    public static final String BAUD_RATE = "baud";
    public static final String BUFFER_SIZE = "buffer";

    private final Logger logger = LoggerFactory.getLogger(SerialBridgeHandler.class);

    private SerialPort serialPort;
    private CommPortIdentifier portId;
    private InputStream inputStream;
    private OutputStream outputStream;
    protected int baud;
    protected int dataBits;
    protected int parity;
    protected int stopBits;
    protected String portName;
    protected int bufferSize;
    protected long sleep = 100;
    protected long interval = 0;
    Thread readerThread = null;

    public SerialBridgeHandler(Bridge thing) {
        super(thing);
    }

    /**
     * Called when data is received on the serial port
     *
     * @param line
     *                 - the received data as a String
     *
     **/
    public abstract void onDataReceived(byte... bytes);

    /**
     * Write data to the serial port
     *
     * @param msg
     *                - the received data as a String
     *
     **/
    public void write(String msg) {
        try {
            // write string to serial port
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error writing '" + msg + "' to serial port " + portName + " : " + e.getMessage());
        }
    }

    /**
     * Write data to the serial port
     *
     * @param msg
     *                - the received data as a String
     *
     **/
    public void write(byte... bytes) {
        try {
            // write string to serial port
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error writing '"
                    + BytesUtil.byteHexString(bytes) + "' to serial port " + portName + " : " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing serial thing handler.");
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        if (serialPort != null) {
            serialPort.close();
        }

        if (readerThread != null) {
            try {
                readerThread.interrupt();
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        try {
            /*
             * The short select() timeout in the native code of the nrjavaserial lib does cause a high CPU load, despite
             * the fix published (see https://github.com/NeuronRobotics/nrjavaserial/issues/22). A workaround for this
             * problem is to (1) put the Thread initiated by the nrjavaserial library to sleep forever, so that the
             * number of calls to the select() function gets minimized, and (2) implement a Threaded streamreader
             * directly in java
             */
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing serial thing handler.");

        if (serialPort == null && portName != null && baud != 0) {
            // parse ports and if the default port is found, initialized the
            // reader
            @SuppressWarnings("rawtypes")
            Enumeration portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (id.getName().equals(portName)) {
                        logger.debug("Serial port '{}' has been found.", portName);
                        portId = id;
                    }
                }
            }

            if (portId != null) {
                // initialize serial port
                try {
                    serialPort = portId.open("openHAB", 2000);
                } catch (PortInUseException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    inputStream = serialPort.getInputStream();
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    serialPort.addEventListener(this);
                } catch (TooManyListenersException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                // activate the DATA_AVAILABLE notifier
                serialPort.notifyOnDataAvailable(true);

                try {
                    // set port parameters
                    serialPort.setSerialPortParams(baud, dataBits, stopBits, parity);
                } catch (UnsupportedCommOperationException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not configure serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    // get the output stream
                    outputStream = serialPort.getOutputStream();
                    updateStatus(ThingStatus.ONLINE);
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not communicate with the serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                readerThread = new SerialPortReader(inputStream);
                readerThread.start();

            } else {
                StringBuilder sb = new StringBuilder();
                portList = CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                    if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        sb.append(id.getName() + "\n");
                    }
                }
                logger.error("Serial port '{}' could not be found. Available ports are:\n {}", portName, sb);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // by default, we write anything we received as a string to the serial
        // port
        write(command.toString());
    }

    public class SerialPortReader extends Thread {

        private boolean interrupted = false;
        private InputStream inputStream;
        private boolean hasInterval = interval == 0 ? false : true;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

        public SerialPortReader(InputStream in) {
            this.inputStream = in;
            this.setName("SerialPortReader-" + getThing().getUID());
        }

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
            try {
                inputStream.close();
            } catch (IOException e) {
            } // quietly close
        }

        @Override
        public void run() {
            byte[] tmpData = new byte[bufferSize];
            int len = -1;

            logger.debug("Serial port listener for serial port '{}' has started", portName);

            try {
                while (!interrupted) {
                    long startOfRead = System.currentTimeMillis();

                    if ((len = inputStream.read(tmpData)) > 0) {
                        onDataReceived(Arrays.copyOf(tmpData, len));
                        if (hasInterval) {
                            try {
                                Thread.sleep(Math.max(interval - (System.currentTimeMillis() - startOfRead), 0));
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                    try {
                        if (sleep > 0) {
                            Thread.sleep(sleep);
                        }
                    } catch (InterruptedException e) {
                    }
                }
            } catch (InterruptedIOException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.error("An exception occurred while reading serial port '{}' : {}", portName, e.getMessage(), e);
            }

            logger.debug("Serial port listener for serial port '{}' has stopped", portName);
        }
    }
}
