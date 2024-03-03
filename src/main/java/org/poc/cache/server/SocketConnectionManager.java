package org.poc.cache.server;

import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketConnectionManager implements ISocketConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(SocketConnectionManager.class);
    private final EnvProperties envProperties;
    private final ConcurrentHashMap<String, Socket> remoteSocketMap = new ConcurrentHashMap<>();

    @Autowired
    public SocketConnectionManager(final EnvProperties envProperties) {
        this.envProperties = envProperties;
    }

    /**
     * remoteAddressMap will change dynamically if balancing occurs in middle of these operations. We keep maintaining all succesfull operations in completed node
     * say after some iterations completedNode ={a,b,c,d} for remoteAddressMapKeySet={a,b,c,d}. Now 'b' drops out of cluster and 'e' joins sometimes when the operation is ongoing.
     * remoteAddressMap = {a,c,d,e} . The condition completedNode.containsAll(remoteAddressMap.keySet()) fails and another attempt is triggered making the map
     * completedNode ={a,b,c,d,e}  Note 'b' is not useful anymore but now completed node contains all elements of remoteAddressMap (vice versa not always true)
     * <p>
     * These are open persistent tcp connections and the map reacts to connection failures - so expecting the transfer to be fast.
     *
     * @param buf
     */
    @Override
    public byte[] publishToCluster(byte[] buf) throws IOException {
        Set<String> completedNode = new HashSet<>();
        int attemptCount = 1;
        int totalAttempts = envProperties.getRemotePublishAttempts();
        while (attemptCount <= totalAttempts) {
            for (Map.Entry<String, Socket> record : remoteSocketMap.entrySet()) {
                if (!record.getKey().equals(envProperties.getLocalNodeIp())) {
                    try {
                        Socket socket = record.getValue();
                        OutputStream outputStream = socket.getOutputStream();
                        InputStream inputStream = socket.getInputStream();
                        if (!completedNode.contains(record.getKey())) {
                            outputStream.write(buf);
                            completedNode.add(record.getKey());
                            byte[] bufReturn = inputStream.readNBytes(envProperties.getBufferLength());
                            if (completedNode.containsAll(remoteSocketMap.keySet())) {
                                return bufReturn;
                            }
                        }


                    } catch (IOException e) {
                        logger.error("Remote write failed for node: {} will retry current retry attempt: {} of out of: {}", record.getKey(), attemptCount, totalAttempts);
                    }
                } else {
                    return new byte[0];
                }
            }
            attemptCount++;
        }
        throw new IOException("Failed to execute operation on cluster - one or more nodes did not accept request");
    }

    @Override
    public byte[] publishToNode(byte[] buf, String node) throws IOException {
        int attemptCount = 1;
        int totalAttempts = envProperties.getRemotePublishAttempts();
        while (attemptCount <= totalAttempts) {
            try {
                Socket socket = remoteSocketMap.get(node);
                boolean connected = socket.isConnected();
                boolean closed = socket.isClosed();
                socket = new Socket(node, envProperties.getNodeServicePort());
                logger.info("Fetch Connection from remoteSocketMap for node: {} connected:  {} , closed: {} ", node, connected, closed);
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                outputStream.write(buf);
                logger.info("wrote {} bytes ", buf.length);
                byte[] bufReturn = inputStream.readNBytes(envProperties.getBufferLength());
                logger.info("read {} bytes ", bufReturn.length);
                logger.info("Print buffer being sent: {} \n received: {}", buf, bufReturn);
                return bufReturn;

            } catch (IOException e) {
                logger.error("Remote write failed for node: {} will retry current retry attempt: {} of out of: {}  exception: {}", node, attemptCount, totalAttempts, e);
                attemptCount++;

            }
        }
        throw new IOException("Failed to execute operation on node = " + node);
    }

    @Override
    public void addConnectionForCluster(String node) {
        try {
            remoteSocketMap.putIfAbsent(node, getConnection(node));
        } catch (IOException e) {
            logger.info("Something wrong when creating or maintaining a socket connection");
        }
    }

    @Override
    public void removeConnectionForCluster(String node) {
        remoteSocketMap.computeIfPresent(node, (a, b) -> {
            try {
                logger.info("Closing the connection to node {}", node);
                b.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public Socket getConnection(String node) throws IOException {
        Socket socket = new Socket(node, envProperties.getNodeServicePort());
        socket.setKeepAlive(true);
        boolean connected = socket.isConnected();
        boolean closed = socket.isClosed();
        logger.info("Creating Connection for node: {} connected:  {} , closed: {} ", node, connected, closed);
        return socket;

    }


    public String printSocketCollection() {
        String ret = "printSocketCollection =>  " + remoteSocketMap.entrySet();
        logger.info(ret);
        return ret;
    }
}
