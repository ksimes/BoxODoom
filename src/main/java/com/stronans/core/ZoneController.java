package com.stronans.core;

import com.stronans.pyroelectric.PyroElectric;
import com.stronans.pyroelectric.PyroElectricListener;
import com.stronans.pyroelectric.PyroElectricName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.pi4j.wiringpi.Gpio.delay;

public class ZoneController implements Runnable, PyroElectricListener {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = LogManager.getLogger(ZoneController.class);
    private final int[] servos;
    private final SerialComms communications;
    private final PyroElectric pyroelectric;
    private boolean finished = false;

    public ZoneController(SerialComms coms, PyroElectric pyroelectric, int[] servos) {
        this.pyroelectric = pyroelectric;
        pyroelectric.addListener(this);
        this.servos = servos;
        this.communications = coms;
    }

    public void shutdown() {
        finished = true;
    }

    public String getName() {
        return pyroelectric.getPyroElectricName().name();
    }

    @Override
    public void run() {
        int count = 0;
        while (!finished) {
//                Thread.sleep(1000);
            delay(500);

            count++;

            if (count > 9) {
                count = 0;
                log.trace("5 second ZoneController thread heartbeat");
            }
        }
    }

    @Override
    public void active(PyroElectricName name) {
        log.info("Zone " + name + " triggered");

        for (int i : servos) {
            String msg = "{ SWP " + i + " 0 }";
            communications.sendMessage(msg);
        }
    }
}
