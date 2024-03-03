package org.poc.cache.server;

import java.io.IOException;

public interface ISocketConnectionManager {

    byte[] publishToCluster(byte[] buf) throws IOException;
    byte[] publishToNode(byte[] buf , String node) throws IOException;
    void addConnectionForCluster(String node);
    void removeConnectionForCluster(String node);
    public String printSocketCollection();
}
