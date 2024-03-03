package org.poc.cache.server;

import org.poc.cache.server.multicast.heartbeat.NodeHeartBeatListener;
import org.poc.cache.server.multicast.heartbeat.NodeHeartBeatManager;
import org.poc.cache.server.multicast.heartbeat.NodeHeartBeatPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class StartMultiCast {

    private final NodeHeartBeatManager nodeHeartBeatManager;

    @Autowired
    public StartMultiCast(final NodeHeartBeatManager nodeHeartBeatManager) {
        this.nodeHeartBeatManager = nodeHeartBeatManager;
    }

    @PostConstruct
    public void startMultiCastProcess() throws IOException, InterruptedException {
        nodeHeartBeatManager.startManger();
        new Thread(new NodeHeartBeatListener(nodeHeartBeatManager)).start();
        new Thread(new NodeHeartBeatPublisher(nodeHeartBeatManager)).start();
    }
}
