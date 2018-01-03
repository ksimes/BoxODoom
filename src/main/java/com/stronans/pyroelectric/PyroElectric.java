package com.stronans.pyroelectric;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A single PyroElectric Detector (PIR) to detect movement.
 * <p>
 * Created by S.King on 31/12/2017.
 */
public class PyroElectric {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = LogManager.getLogger(PyroElectric.class);
    private PyroElectricName gpioPin;
    private List<PyroElectricListener> listeners = new ArrayList<>();

    public PyroElectric(PyroElectricName gpioPin, final GpioController gpio) {

        this.gpioPin = gpioPin;

        // provision gpio pin as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput thisPin = gpio.provisionDigitalInputPin(gpioPin.getVal(), PinPullResistance.PULL_DOWN);

        // set shutdown state for this input pin
        thisPin.setShutdownOptions(true);

        // create and register gpio pin listener
        thisPin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // Pyroelectric goes high when it becomes active
                if (event.getState().isHigh()) {
                    notifyListeners(gpioPin);
                }

                // display pin state in logger
                log.trace(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
            }
        });
    }

    public PyroElectricName getPyroElectricName() {
        return gpioPin;
    }

    public boolean addListener(PyroElectricListener listener) {
        boolean result = false;

        listeners.add(listener);

        return result;
    }

    private void notifyListeners(PyroElectricName gpioPin) {
        log.trace(" Notify : " + gpioPin.toString());

        if (!listeners.isEmpty()) {
            for (PyroElectricListener listener : listeners) {
                listener.active(gpioPin);
            }
        }
    }
}
