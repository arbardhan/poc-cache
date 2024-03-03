package org.poc.cache.server.multicast.heartbeat;

import java.io.IOException;

public class NodeHeartBeatListener implements Runnable {
    private final NodeHeartBeatManager nodeHeartBeatManager;

    public NodeHeartBeatListener(NodeHeartBeatManager nodeHeartBeatManager) {
        this.nodeHeartBeatManager = nodeHeartBeatManager;
    }

    @Override
    public void run() {
        try {
            nodeHeartBeatManager.updateHeartBeatFromMulticast();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
