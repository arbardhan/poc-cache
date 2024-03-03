package org.poc.cache.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;

@Component
public class EnvProperties {

    private static final Logger logger = LoggerFactory.getLogger(EnvProperties.class);

    private final String heartBeatMulticastGroup;
    private final int heartBeatMulticastPort;
    private final String activeNodeMulticastGroup;
    private final int activeNodeMulticastPort;
    private final String keyNodeMappingMulticastGroup;
    private final int keyNodeMappingMulticastPort;
    private final int nodeServicePort;
    private final int remotePublishAttempts;
    private final long nodeStartUpTimeUTC;
    private final int bufferLength;
    private final String localNodeIp;
    private final int cacheEntryLifeMillis;
    private final int multicastPublishIntervalMillis;
    private final int multiCastCommQueueLength;
    private final int nativeThreadPoolSize;
    private final int remoteThreadPoolSize;

    @Autowired
    public EnvProperties(
            @Value("${node.heartbeat.multicast.group}") String heartBeatMulticastGroup,
            @Value("${node.heartbeat.multicast.port}") int heartBeatMulticastPort,
            @Value("${node.active.multicast.group}") String activeNodeMulticastGroup,
            @Value("${node.active.multicast.port}") int activeNodeMulticastPort,
            @Value("${node.key.mapping.multicast.group}") String keyNodeMappingMulticastGroup,
            @Value("${node.key.mapping.multicast.port}") int keyNodeMappingMulticastPort,
            @Value("${node.service.port}") int nodeServicePort,
            @Value("${node.service.remote.attempts}") int remotePublishAttempts,
            @Value("${node.buffer.length}") int bufferLength,
            @Value("${node.cache.entry.life.millis}") int cacheEntryLifeMillis,
            @Value("${node.multicast.publish.frequency.millis}") int multicastPublishIntervalMillis,
            @Value("${node.multicast.queue.length}") int multiCastCommQueueLength,
            @Value("${node.native.request.threadPool}") int nativeThreadPoolSize,
            @Value("${node.remote.request.threadPool}") int remoteThreadPoolSize) throws UnknownHostException {
        this.heartBeatMulticastGroup = heartBeatMulticastGroup;
        this.heartBeatMulticastPort=heartBeatMulticastPort;
        this.activeNodeMulticastGroup = activeNodeMulticastGroup;
        this.activeNodeMulticastPort=activeNodeMulticastPort;
        this.keyNodeMappingMulticastGroup = keyNodeMappingMulticastGroup;
        this.keyNodeMappingMulticastPort=keyNodeMappingMulticastPort;
        this.nodeServicePort=nodeServicePort;
        this.remotePublishAttempts=remotePublishAttempts;
        this.bufferLength=bufferLength;
        this.nodeStartUpTimeUTC =LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        this.localNodeIp =getLocalIP();
        this.cacheEntryLifeMillis=cacheEntryLifeMillis;
        this.multicastPublishIntervalMillis=multicastPublishIntervalMillis;
        this.multiCastCommQueueLength=multiCastCommQueueLength;
        this.nativeThreadPoolSize=nativeThreadPoolSize;
        this.remoteThreadPoolSize=remoteThreadPoolSize;
    }

    public String getLocalNodeIp() throws UnknownHostException {
        return localNodeIp;
    }

    public String getHeartBeatMulticastGroup(){
        return this.heartBeatMulticastGroup;
    }

    public int getHeartBeatMulticastPort(){
        return this.heartBeatMulticastPort;
    }

    public String getActiveNodeMulticastGroup(){
        return this.activeNodeMulticastGroup;
    }

    public int getActiveNodeMulticastPort(){
        return this.activeNodeMulticastPort;
    }

    public String getKeyNodeMappingMulticastGroup(){
        return this.keyNodeMappingMulticastGroup;
    }
    public int getKeyNodeMappingMulticastPort(){
        return this.keyNodeMappingMulticastPort;
    }

    public long getNodeStartUpTimeUTC(){
        return this.nodeStartUpTimeUTC;
    }

    public int getNodeServicePort() {
        return nodeServicePort;
    }

    public int getRemotePublishAttempts() {
        return remotePublishAttempts;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public int getCacheEntryLifeMillis() {
        return cacheEntryLifeMillis;
    }

    public int getMulticastPublishIntervalMillis() {
        return multicastPublishIntervalMillis;
    }

    public int getMultiCastCommQueueLength() {
        return multiCastCommQueueLength;
    }

    public int getNativeThreadPoolSize() {
        return nativeThreadPoolSize;
    }

    public int getRemoteThreadPoolSize() {
        return remoteThreadPoolSize;
    }

    private String  getLocalIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        logger.info("Local IPv4 address: {}" , address.getHostAddress());
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        logger.info("NO IP FOUND");
        return null;
    }
}
