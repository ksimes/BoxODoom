package com.stronans.core;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.stronans.Application;
import com.stronans.pyroelectric.PyroElectric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.pi4j.wiringpi.Gpio.delay;
import static com.stronans.pyroelectric.PyroElectricName.*;

public class BoxOfDoom {
    private static final String SERVO_CONTROLLER = "/dev/ttyUSB0";
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = LogManager.getLogger(Application.class);
    //    private static final String LED_NANO = "/dev/ttyUSB1";
    private static SerialComms ServoControllerNano;

    public BoxOfDoom() {
        try {
            // Initialise GPIO connection
            // create gpio controller
            final GpioController gpio = GpioFactory.getInstance();

            ServoControllerNano = new SerialComms(SERVO_CONTROLLER);
            ServoControllerNano.startComms();

            // Setup pyroelectric
            // All Zones to return activations to their own class
            List<ZoneController> controllers = new ArrayList<>();
            int[] zone1Servos = {0, 1, 2, 3};
            controllers.add(new ZoneController(ServoControllerNano, new PyroElectric(ZONE1, gpio), zone1Servos));
            int[] zone2Servos = {4, 5, 6, 7};
            controllers.add(new ZoneController(ServoControllerNano, new PyroElectric(ZONE2, gpio), zone2Servos));
            int[] zone3Servos = {8, 9, 10, 11};
            controllers.add(new ZoneController(ServoControllerNano, new PyroElectric(ZONE3, gpio), zone3Servos));
            int[] zone4Servos = {12, 13, 14, 15};
            controllers.add(new ZoneController(ServoControllerNano, new PyroElectric(ZONE4, gpio), zone4Servos));

            // Start the zones as their own threads to avoid complications.
            for (ZoneController zone : controllers) {
                Thread zoneThread = new Thread(zone, zone.getName());
                zoneThread.start();
            }

            // Housekeeping
            Runtime.getRuntime().addShutdownHook(new Thread() {
                                                     @Override
                                                     public void run() {
                                                         try {
                                                             for (ZoneController zone : controllers) {
                                                                 zone.shutdown();
                                                             }

                                                             ServoControllerNano.endComms();
                                                             gpio.shutdown();   // <--- implement this method call if you wish to terminate the Pi4J GPIO controller
                                                         } catch (Exception e) {
                                                             log.error("Error: ", e);
                                                         } finally {
                                                             log.info("Exiting program.");
                                                         }
                                                     }
                                                 }

            );

            log.info("Everything now setup and running");

        } catch (InterruptedException e) {
            log.error(" ==>> PROBLEMS WITH SERIAL COMMUNICATIONS: " + e.getMessage(), e);
        }
    }

    public void start() throws InterruptedException {
        int count = 0;
        while (true) {
//                Thread.sleep(1000);
            delay(1000);

            count++;

            if (count > 4) {
                count = 0;
                log.trace("5 second main thread heartbeat");
            }

            // Replies from the Ardunio Nano to be logger
            if (ServoControllerNano.messagesAvailable()) {
                try {
                    log.info("Message back : " + ServoControllerNano.getMessage().trim());
                } catch (InterruptedException e) {
                    log.error("Error: ", e);
                }
            }
        }
    }
}

