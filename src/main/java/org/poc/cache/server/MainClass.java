package org.poc.cache.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;


public class MainClass {

    private static final Logger logger = LoggerFactory.getLogger(MainClass.class);
    public static void main(String args[]) throws IOException {
        logger.info("Starting Server Now");
        ConfigurableApplicationContext context = SpringApplication.run(MainInterface.class, args);
        logger.info(" Environment {} ",context.getEnvironment());
        CacheServer cacheServer = (CacheServer)context.getBean("cacheServer");
        logger.info("Starting up cache");
        cacheServer.beginServerLoop();
        logger.info("Server Loop ends Started");

    }
}
