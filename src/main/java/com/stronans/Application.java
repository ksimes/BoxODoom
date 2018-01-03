package com.stronans;

import com.stronans.core.BoxOfDoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = LogManager.getLogger(Application.class);

    public static void main(String args[]) throws Exception {
        log.info("Box o' Doooom ... started.");
        BoxOfDoom boxOfDoom = new BoxOfDoom();
        boxOfDoom.start();
    }
}
