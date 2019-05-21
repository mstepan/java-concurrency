package com.max.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Main() throws Exception {


        LOG.info("[main] done... java-" + System.getProperty("java.version"));
    }

    public static void main(String[] args) {
        try {
            new Main();
        }
        catch (Exception ex) {
            LOG.error("Error occurred", ex);
        }
    }
}
