package org.poc.cache.server;

import org.apache.commons.lang3.tuple.Pair;
import org.poc.cache.server.multicast.MulticastCommQueue;
import org.poc.cache.server.multicast.heartbeat.ConsistentHashManager;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Current attempt is to simply have a reference each of multicast manager of storage node and keys to see what node is in what key.
 * These collections get queried very frequently - hence would need to research of the impact on Objects who hold their reference . In my view
 * it should not be a problem
 */
@Component
@ManagedResource
public class NodeManager {

    private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);
    private final EnvProperties envProperties;
    private final ConsistentHashManager consistentHashManager;
    private final MulticastCommQueue multicastCommQueue;
    private final ISocketConnectionManager socketConnectionManager;

    @Autowired
    public NodeManager(final EnvProperties envProperties,
                       final ConsistentHashManager consistentHashManager,
                       final MulticastCommQueue multicastCommQueue,
                       final ISocketConnectionManager socketConnectionManager) {
        this.envProperties = envProperties;
        this.consistentHashManager = consistentHashManager;
        this.multicastCommQueue = multicastCommQueue;
        this.socketConnectionManager = socketConnectionManager;
    }

    @PostConstruct
    public void listenToQueue() {
        Executors.newSingleThreadExecutor().submit(() -> {
            logger.info("Starting multicastCommQueue queue listener");
            while (true) {
                Pair<String, MulticastCommQueue.MultiCastQueueEventType> element = multicastCommQueue.take();
                String node = element.getLeft();
                switch (element.getRight()) {
                    case REMOVE: {
                        removeNode(node);
                    }
                    case ADD: {
                        addNode(node);
                    }
                }
            }
        });
    }


    public void addNode(String node) throws UnknownHostException {
        socketConnectionManager.addConnectionForCluster(node);
        consistentHashManager.addNode(node);
    }

    public void removeNode(String node) throws UnknownHostException {
        socketConnectionManager.removeConnectionForCluster(node);
        consistentHashManager.removeNode(node);
    }

    public void populateKeyNodeMapping(Set<String> keys) {
        consistentHashManager.populateKeyNodeMapping(keys);
    }

    @ManagedOperation
    public Optional<String> getNodeAddressForKey(String key) {
        return consistentHashManager.getNodeMappedToKey(key);
    }

    @ManagedOperation
    public String getCurrentNodeAddress() throws UnknownHostException {
        return envProperties.getLocalNodeIp();
    }

    public byte[] publishToCluster(byte[] buf) throws IOException {
        return socketConnectionManager.publishToCluster(buf);
    }

    public byte[] publishToNode(byte[] buf, String node) throws IOException {
        return socketConnectionManager.publishToNode(buf, node);
    }

    @ManagedOperation
    public String printNodeHashSpace() {
        return consistentHashManager.printNodeHashSpace();
    }

    @ManagedOperation

    public String printKeyNodeMapping() {
        return consistentHashManager.printKeyNodeMapping();
    }

    @ManagedOperation
    public String printSocketCollection() {
        return socketConnectionManager.printSocketCollection();
    }

}
