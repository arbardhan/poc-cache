package org.poc.cache.server.multicast.heartbeat;

import java.io.IOException;

public class NodeHeartBeatPublisher implements Runnable{

    private final NodeHeartBeatManager nodeHeartBeatManager;

    public NodeHeartBeatPublisher(NodeHeartBeatManager nodeHeartBeatManager){
        this.nodeHeartBeatManager=nodeHeartBeatManager;
    }

    @Override
    public void run() {
        try {
            nodeHeartBeatManager.publishHeartBeat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
