package org.poc.cache.server;

import org.poc.cache.server.operation.DummySocket;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.Socket;

@Component
public class DummySocketConnectionManager extends SocketConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DummySocketConnectionManager.class);
    private EnvProperties envProperties;

    @PostConstruct
    public void init(){
        logger.info("bean for DummySocketConnectionManager has been created");
    }

    @Autowired
    public DummySocketConnectionManager(EnvProperties envProperties) {
        super(envProperties);
        this.envProperties=envProperties;
    }

    @Override
    public Socket getConnection(String node) throws IOException {
        logger.info("DummySocketConnectionManager creating dummy connection for : {}",node);
        try (Socket socket = new DummySocket(node, envProperties.getNodeServicePort())) {
            socket.setKeepAlive(true);
            return socket;
        }
    }
}
