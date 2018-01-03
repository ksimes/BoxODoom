package com.stronans.pyroelectric;

/**
 * Created by S.King on 15/02/2017.
 */
public interface PyroElectricListener {

    // When a Pyroelectric is active then the signal goes high
    void active(PyroElectricName name);
}
