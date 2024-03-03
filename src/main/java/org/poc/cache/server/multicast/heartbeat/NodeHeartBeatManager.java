package org.poc.cache.server.multicast.heartbeat;

import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.poc.cache.server.multicast.MulticastCommQueue;
import org.poc.cache.server.proto.NodeHeartBeatOuterClass.NodeHeartBeat;
import org.poc.cache.server.utils.ByteManipulationUtils;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NodeHeartBeatManager {

    private static final Logger logger = LoggerFactory.getLogger(NodeHeartBeatManager.class);
    private final Map<String, NodeHeartBeat> nodeHeartBeatMap;
    private final AtomicInteger nodeHeartBeatSequence = new AtomicInteger();
    private MulticastSocket multicastSocket;
    private final EnvProperties envProperties;
    private final ConsistentHashManager consistentHashManager;
    private final MulticastCommQueue multicastCommQueue;

    @Autowired
    public NodeHeartBeatManager(final EnvProperties envProperties, final ConsistentHashManager consistentHashManager, final MulticastCommQueue multicastCommQueue) {
        this.multicastCommQueue = multicastCommQueue;
        this.consistentHashManager = consistentHashManager;
        this.envProperties = envProperties;
        this.nodeHeartBeatMap = ExpiringMap.builder()
                .expiration(envProperties.getCacheEntryLifeMillis(), TimeUnit.MILLISECONDS)
                .expirationListener((ExpirationListener<String, NodeHeartBeat>) (key, value) -> {
                    logger.info("Key {} has expired ", key);
                    consistentHashManager.removeNode(key);
                    try {
                        multicastCommQueue.put(Pair.of(key, MulticastCommQueue.MultiCastQueueEventType.REMOVE));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

    }

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        startManger();
        updateHeartBeatFromMulticast();
        publishHeartBeat();
    }

    public void startManger() throws IOException {
        multicastSocket = new MulticastSocket(envProperties.getHeartBeatMulticastPort());
        multicastSocket.joinGroup(InetAddress.getByName(envProperties.getHeartBeatMulticastGroup()));
    }


    public synchronized void publishHeartBeat() throws IOException, InterruptedException {

        Executors.newSingleThreadExecutor().submit(()-> {
        while (true) {
            publishPacket(createNodeHeartBeat(nodeHeartBeatMap.keySet()));
            Thread.sleep(envProperties.getMulticastPublishIntervalMillis());
        }
        });
    }

    public synchronized void publishHeartBeat(int numberOfBeats) throws IOException, InterruptedException {
        Validate.isTrue(numberOfBeats > 0);
        while (numberOfBeats > 0) {
            publishPacket(createNodeHeartBeat(nodeHeartBeatMap.keySet()));
            Thread.sleep(envProperties.getMulticastPublishIntervalMillis());
            numberOfBeats--;
        }
    }

    public synchronized void updateHeartBeatFromMulticast() throws IOException, InterruptedException {
        Executors.newSingleThreadExecutor().submit(()-> {
            while (true) {
                NodeHeartBeat nodeHeartBeat = getNodeHeartBeatFromByteStream();
                if (null == nodeHeartBeatMap.get(nodeHeartBeat.getHeartBeatSource())) {
                    consistentHashManager.addNode(nodeHeartBeat.getHeartBeatSource());
                    multicastCommQueue.put(Pair.of(nodeHeartBeat.getHeartBeatSource(), MulticastCommQueue.MultiCastQueueEventType.ADD));
                }
                nodeHeartBeatMap.merge(nodeHeartBeat.getHeartBeatSource(), nodeHeartBeat, (existingBeat, newBeat) -> {
                    if (existingBeat.getHeartBeatSequence() < newBeat.getHeartBeatSequence()) {
                        if (existingBeat.getHeartBeatStartTime() == newBeat.getHeartBeatStartTime()) {
                            return newBeat;
                        }
                    } else {
                        if (existingBeat.getHeartBeatStartTime() < newBeat.getHeartBeatStartTime()) {
                            return newBeat;
                        }
                    }
                    return existingBeat;
                });
            }
        });
    }

    private int incrementAndGetHeartBeat() {
        return nodeHeartBeatSequence.incrementAndGet();
    }

    private synchronized NodeHeartBeat createNodeHeartBeat(Set<String> nodeList) throws UnknownHostException {
        return NodeHeartBeat.newBuilder().setHeartBeatStartTime(envProperties.getNodeStartUpTimeUTC()).setHeartBeatSequence(incrementAndGetHeartBeat()).setHeartBeatSource(envProperties.getLocalNodeIp()).addAllLiveNodes(nodeList).build();
    }

    private void publishPacket(NodeHeartBeat nodeHeartBeat) throws IOException {
        byte[] senderFrame = ByteManipulationUtils.createFrame(nodeHeartBeat);
        DatagramPacket packetToBeSent = new DatagramPacket(senderFrame, senderFrame.length, InetAddress.getByName(envProperties.getHeartBeatMulticastGroup()), envProperties.getHeartBeatMulticastPort());
        //logger.info("publishPacket nodeHeartBeat sending: {} ", nodeHeartBeat);
        multicastSocket.send(packetToBeSent);
    }

    private NodeHeartBeat getNodeHeartBeatFromByteStream() throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(new byte[envProperties.getBufferLength()], envProperties.getBufferLength(), InetAddress.getByName(envProperties.getHeartBeatMulticastGroup()), envProperties.getHeartBeatMulticastPort());
        multicastSocket.receive(datagramPacket);
        byte[] buf = datagramPacket.getData();
        NodeHeartBeat nodeHeartBeat = ByteManipulationUtils.deSerializeProto(buf, NodeHeartBeat.parser());
        //logger.info("getNodeHeartBeatFromByteStream {}",nodeHeartBeat);
        return nodeHeartBeat;
    }

    public synchronized Map<String, NodeHeartBeat> getNodeHeartBeatMap() {
        return nodeHeartBeatMap;
    }
}
