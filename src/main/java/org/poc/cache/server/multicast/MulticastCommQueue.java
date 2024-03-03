package org.poc.cache.server.multicast;

import org.apache.commons.lang3.tuple.Pair;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class MulticastCommQueue {

    private static final Logger logger = LoggerFactory.getLogger(MulticastCommQueue.class);
    private final BlockingQueue<Pair<String, MultiCastQueueEventType>> multicastCommQueue;
    private final EnvProperties envProperties;

    public MulticastCommQueue(final EnvProperties envProperties) {
        this.envProperties = envProperties;
        this.multicastCommQueue = new ArrayBlockingQueue<>(envProperties.getMultiCastCommQueueLength());

    }


    public void put(Pair<String, MultiCastQueueEventType> element) throws InterruptedException {
        logger.info("add to multicastCommQueue element: {}", element);
        multicastCommQueue.put(element);
    }

    public Pair<String, MultiCastQueueEventType> take() throws InterruptedException {

        Pair<String, MultiCastQueueEventType> element = multicastCommQueue.take();
        logger.info("took from multicastCommQueue element: {}", element);
        return element;
    }


    public Pair<String, MultiCastQueueEventType> peek() {
        return multicastCommQueue.peek();
    }


    public void clear() {
        multicastCommQueue.clear();
    }

    public void drainAndPrintQueue() {

        List<Pair<String, MultiCastQueueEventType>> drainAndPrintList = new ArrayList<>();
        multicastCommQueue.drainTo(drainAndPrintList);
        logger.info("drainAndPrint multicastCommQueue {} ", drainAndPrintList);
    }

    public enum MultiCastQueueEventType {ADD, REMOVE;}

}


