package org.poc.cache.server.operation;

import org.poc.cache.server.CacheServer;
import org.poc.cache.server.proto.OperationRequestOuterClass.OperationRequest;
import org.poc.cache.server.proto.OperationResponseOuterClass.OperationResponse;
import org.poc.cache.server.utils.ByteManipulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class OperationExecutor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CacheServer.class);
    private final OperationInterceptor operationInterceptor;
    private final Socket socket;

    public OperationExecutor(OperationInterceptor operationInterceptor, Socket socket) {
        logger.info("Runnable constructor called with {} ,{}",operationInterceptor,socket);
        this.operationInterceptor = operationInterceptor;
        this.socket = socket;

    }

    @Override
    public void run() {

        OperationResponse operationResponse = null;
        try {
            logger.info("Current Thread is {}",Thread.currentThread().getName());
            OperationRequest operationRequest = operationInterceptor.extractOperationRequest(socket);
            operationResponse = operationInterceptor.handleIncomingRequest(operationRequest);
            logger.info("OperationExecutor operationResponse: {} ", operationResponse);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(ByteManipulationUtils.createFrame(operationResponse));
            outputStream.flush();
            logger.info("Closing Client Socket");
            socket.close();
        } catch (IOException | IllegalAccessException e) {
            logger.error("OperationExecutor Caught exception ", e);
            //throw new RuntimeException(e);
        }

    }

}
