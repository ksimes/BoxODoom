package com.stronans.core;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Serial communications.
 * Created by S.King on 21/05/2016.
 * Much improved S.King on 13/01/2018.
 */
public class SerialComms {

    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = LogManager.getLogger(SerialComms.class);
    private final Serial serial;
    private String comPort = Serial.DEFAULT_COM_PORT;
    private int speed = 9600;     // Default com speed
    private final String terminator = "\r";
    private final char terminatorchar = '\r';

    private String message;
    private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<>(20);
    private List<MessageListener> listeners = new ArrayList<>();

    public SerialComms(String port, int speed) throws InterruptedException {
        if (port != null) {
            comPort = port;
        }

        if (speed != 0) {
            this.speed = speed;
        }

        // create an instance of the serial communications class
        serial = SerialFactory.createInstance();

        // create and register the serial data listener
        serial.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                try {
                    message += event.getString(Charset.defaultCharset());
                } catch (IOException e) {
                    log.error("Cannot convert text from : " + comPort + " : " + e.getMessage(), e);
                }
//                log.info("Pi received : [" + message + "]");

                while (message.contains(terminator)) {
                    messages.add(message.substring(0, message.indexOf(terminatorchar)));
                    notifyListeners();
                    message = message.substring(message.indexOf(terminatorchar) + 1);
                }
            }
        });
    }

    public SerialComms(String port) throws InterruptedException {
        this(port, 0);
    }

    public SerialComms() throws InterruptedException {
        this(null, 0);
    }

    public String getPort() {
        return comPort;
    }

    public int getSpeed() {
        return speed;
    }

    public ArrayBlockingQueue<String> messages() {
        return messages;
    }

    public synchronized String getMessage() throws InterruptedException {
        return messages().take();
    }

    public synchronized boolean messagesAvailable() {
        return !messages().isEmpty();
    }

    public synchronized void sendMessage(String message) {
        try {
            log.info("Message Sent : [" + message + "] port :" + comPort);
            message = message + "\r";
            serial.write(message);
        } catch (IOException ex) {
            log.error(" ==>> SERIAL WRITE FAILED on port : " + comPort + " : " + ex.getMessage(), ex);
        }
    }

    public void startComms() {
        try {
            // open the default serial port provided on the GPIO header
            serial.open(comPort, speed);
        } catch (IOException ex) {
            log.error(" ==>> SERIAL SETUP FAILED on port : " + comPort + " : " + ex.getMessage(), ex);
        }
    }

    public void endComms() {
        try {
            // open the default serial port provided on the GPIO header
            if (serial.isOpen()) {
                serial.close();
            }
        } catch (IOException ex) {
            log.error(" ==>> SERIAL SHUTDOWN FAILED on port : " + comPort + " : " + ex.getMessage(), ex);
        }
    }

    boolean addListener(MessageListener listener) {
        boolean result = false;

        listeners.add(listener);

        return result;
    }

    private void notifyListeners() {
        if (!listeners.isEmpty()) {
            for (MessageListener listener : listeners) {
                listener.messageReceived();
            }
        }
    }
}