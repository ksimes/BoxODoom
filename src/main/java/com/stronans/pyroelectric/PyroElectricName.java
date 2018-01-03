package com.stronans.pyroelectric;

import com.pi4j.io.gpio.Pin;

import static com.pi4j.io.gpio.RaspiPin.*;

/**
 * Created by S.King on 27/12/2017.
 */
public enum PyroElectricName {
    ZONE1(GPIO_02),
    ZONE2(GPIO_03),
    ZONE3(GPIO_04),
    ZONE4(GPIO_05);

    private Pin gpioPin;

    PyroElectricName(Pin numVal) {
        this.gpioPin = numVal;
    }

    public Pin getVal() {
        return gpioPin;
    }
}
