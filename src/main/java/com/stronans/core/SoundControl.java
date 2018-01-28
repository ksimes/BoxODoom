package com.stronans.core;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mp3transform.Decoder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class SoundControl {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = LogManager.getLogger(SoundControl.class);
//    private static Player player;

    static public void playSound(String fileName) {
//        File file;
//        FileInputStream fis;
//
//        try {
//            URL url = SoundControl.class.getResource("/" + fileName);
//            log.info("url : " + url);
//            file = new File(url.getFile());
//            file = new File("./sounds/" + fileName);
//            log.info("found file : " + file.getAbsolutePath());
//
//            fis = new FileInputStream(file);
//            BufferedInputStream bis = new BufferedInputStream(fis);
//            player = new Player(bis);
//        } catch (JavaLayerException e) {
//            log.error("JavaLayerException: ", e);
//        } catch (IOException e) {
//            log.error("IOException: ", e);
//        }
//
//        // run in new thread to play in background
//        new Thread(() -> {
//            try {
//                log.info("playing");
//                player.play(); }
//            catch (Exception e) { log.error("Exception: ", e); }
//        }).start();


        Thread t1 = new Thread(new Runnable() {
            public void run() {
                try {
                    Decoder decoder = new Decoder();
                    File file = new File("sounds/" + fileName);
                    FileInputStream in = new FileInputStream(file);
                    BufferedInputStream bin = new BufferedInputStream(in, 128 * 1024);
                    decoder.play(file.getName(), bin);
                    in.close();

                    decoder.stop();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.out.println("Failed to play the file.");
                }
            }
        });
        t1.start();
    }
}
