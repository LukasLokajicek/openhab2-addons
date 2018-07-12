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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.forcomfort.physical.ByteArrayListener;
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
public class SerialHandler implements SerialPortEventListener {

    // List of all Configuration parameters
    public static final String PORT = "port";
    public static final String BAUD_RATE = "baud";
    public static final String BUFFER_SIZE = "buffer";

    private final Logger logger = LoggerFactory.getLogger(SerialHandler.class);

    private SerialPort serialPort;
    private CommPortIdentifier portId;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int baud = 3840;
    private int dataBits = 8;
    private int parity = 0;
    private int stopBits = 1;
    private String portName;
    private int bufferSize;
    private long sleep = 100;
    private long interval = 0;
    private Thread readerThread = null;
    @NonNull
    private final ByteArrayListener listener;

    public SerialHandler(@NonNull ByteArrayListener listener) {
        this.listener = listener;
    }

    /**
     * Write data to the serial port
     *
     * @param msg
     *                - the received data as a String
     *
     **/
    public UpdateStatusHolder write(String msg) {
        try {
            // write string to serial port
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error writing '" + msg + "' to serial port " + portName + " : " + e.getMessage());
        }
        return null;
    }

    /**
     * Write data to the serial port
     *
     * @param msg
     *                - the received data as a String
     *
     **/
    public UpdateStatusHolder write(byte... bytes) {
        try {
            // write string to serial port
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error writing '"
                    + BytesUtil.byteHexString(bytes) + "' to serial port " + portName + " : " + e.getMessage());
        }
        return null;
    }

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

    public UpdateStatusHolder initialize() {
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
                    return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                }

                try {
                    inputStream = serialPort.getInputStream();
                } catch (IOException e) {
                    return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                }

                try {
                    serialPort.addEventListener(this);
                } catch (TooManyListenersException e) {
                    return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                }

                // activate the DATA_AVAILABLE notifier
                serialPort.notifyOnDataAvailable(true);

                try {
                    // set port parameters
                    serialPort.setSerialPortParams(baud, dataBits, stopBits, parity);
                } catch (UnsupportedCommOperationException e) {
                    return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not configure serial port " + serialPort + ": " + e.getMessage());
                }

                try {
                    // get the output stream
                    outputStream = serialPort.getOutputStream();
                } catch (IOException e) {
                    return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not communicate with the serial port " + serialPort + ": " + e.getMessage());
                }

                readerThread = new SerialPortReader(inputStream);
                readerThread.start();

            } else {
                StringBuilder portNames = new StringBuilder();
                portList = CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                    if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        portNames.append(id.getName() + "\n");
                    }
                }
                StringBuilder errorMessage = new StringBuilder("Serial port ");
                errorMessage.append(portName);
                errorMessage.append(" could not be found. Available ports are:\n ");
                errorMessage.append(portNames);
                logger.error(errorMessage.toString());
                return new UpdateStatusHolder(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        errorMessage.toString());
            }
        }
        return null;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        this.baud = baud;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public class SerialPortReader extends Thread {

        private boolean interrupted = false;
        private InputStream inputStream;
        private boolean hasInterval = interval == 0 ? false : true;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

        public SerialPortReader(InputStream in) {
            this.inputStream = in;
            this.setName("SerialPortReader - " + portName);
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
                        listener.onDataReceived(Arrays.copyOf(tmpData, len));
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

    public void setParametres(String portName, int baud, int dataBits, int parity, int stopBits, int bufferSize,
            int sleep) {
        this.portName = portName;
        this.baud = baud;
        this.dataBits = dataBits;
        this.parity = parity;
        this.stopBits = stopBits;
        this.bufferSize = bufferSize;
        this.sleep = sleep;
    }
}
