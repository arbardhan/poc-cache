package org.poc.cache.server.operation;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.poc.cache.server.NodeManager;
import org.poc.cache.server.proto.OperationRequestOuterClass.OperationRequest;
import org.poc.cache.server.proto.OperationRequestTypeOuterClass.OperationRequestType;
import org.poc.cache.server.proto.OperationResponseOuterClass.OperationResponse;
import org.poc.cache.server.utils.ByteManipulationUtils;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Incoming requests from clients (includes other servers as well) are negotiated as follows
 *
 * <ul>
 *     <li>ALTER</li>
 *     <P>
 *         Runs first on the primary node of the key. After success a distribution request is sent to other nodes.
 *
 *     </P>
 *     <li>FETCH</li>
 *     <p>
 *         Runs on the primary node of the key only.
 *     </p>
 *     <li>EXISTS</li>
 *     <p>
 *         Runs on the primary node of the key only.
 *     </p>
 *     <li>CLEAR</li>
 *     <p>
 *         Runs first on local, then on other servers as a distribution request.
 *     </p>
 *     <li>DISTRIBUTION</li>
 *     <p>
 *         Runs on local when received.
 *
 *     </p>
 *
 * </ul>
 */

@Component
public class OperationInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OperationInterceptor.class);
    private final List<IOperationManager> operationManagers = new CopyOnWriteArrayList<>();
    private final NodeManager nodeManager;
    private final EnvProperties envProperties;
    //private final CompletionService nativeThreadPool;
    //private final CompletionService remoteThreadPool;

    @Autowired
    public OperationInterceptor(NodeManager nodeManager, final EnvProperties envProperties) {
        this.nodeManager = nodeManager;
        this.envProperties = envProperties;
        //this.nativeThreadPool = new ExecutorCompletionService(Executors.newFixedThreadPool(envProperties.getNativeThreadPoolSize()));
        //this.remoteThreadPool = new ExecutorCompletionService(Executors.newFixedThreadPool(envProperties.getRemoteThreadPoolSize()));
    }

    public void addOperationManager(IOperationManager operationManager) {
        this.operationManagers.add(operationManager);
    }


    public OperationResponse handleIncomingRequest(OperationRequest operationRequest) throws IllegalAccessException, IOException {
        validateOperationRequest(operationRequest);
        OperationResponse operationResponse = null;
        OperationType operationType = OperationType.valueOf(operationRequest.getOperationRequestType().name());
        logger.info("handleIncomingRequest operationRequest: {}  operationType: {} ", operationRequest, operationType);
        if (operationType == null) {
            throw new IllegalArgumentException("Invalid Operation Type for Request " + operationRequest);
        }
        switch (operationType) {
            case ALTER: {
                operationResponse = getResponseForAlter(operationRequest);
                break;
            }
            case FETCH:
            case EXISTS: {
                operationResponse = getResponseForFetchAndExist(operationRequest);
                break;
            }
            case DISTRIBUTION: {
                operationResponse = getResponseForDistribution(operationRequest);
                break;
            }
            case CLEAR: {
                operationResponse = getResponseForClear(operationRequest);
                break;
            }
        }
        return operationResponse;
    }

    private OperationResponse process(OperationRequest operationRequest) throws IllegalAccessException {
        logger.info("Local node will process operationRequest: {}", operationRequest);
        for (IOperationManager manager : operationManagers) {
            if (manager.getOperationType().name().
                    equals(operationRequest.getOperationRequestType().name())) {
                return manager.process(operationRequest);
            }
        }
        throw new IllegalArgumentException("No valid processor found for request: " + operationRequest);
    }

    /**
     * Extract the key if applicable and determine if the key belongs to current server <br>
     * Determine the nature of operation from Operation Type <br>
     * For Alter  if local then call the interceptor methods. Post put on a separate thread as REMOTE request
     * to others -- other recipients when seeing remote will apply data to their own local knowing its being orchestrated already
     * <p>
     * Pending work for 14 Dec -- create the payload, create the orchestration thread system.
     */

    private OperationResponse executeOnCluster(OperationRequest operationRequest) throws IOException {
        return OperationResponse.parseFrom(nodeManager.publishToCluster(createNetworkPayload(operationRequest)));
    }


    public OperationRequest extractOperationRequest(Socket clientSocket) {
        OperationRequest operationRequest = null;
        try {
            InputStream inputStream = clientSocket.getInputStream();
            byte[] buf = new byte[envProperties.getBufferLength()];
            inputStream.read(buf);
            operationRequest = ByteManipulationUtils.deSerializeProto(buf, OperationRequest.parser());
            logger.info("extractOperationRequest operationRequest {} ", operationRequest.toString());

        } catch (IOException e) {
            logger.error("extractOperationRequest parsing error {}", e);

        } finally {
            return operationRequest;
        }
    }

    /**
     * Data Element With Key—>  [TOTAL MESSAGE LENGTH][OPERATION REQUEST SERIALIZED -- CONTAINS BOTH  KEY AND DATA] <br>
     * This is where we modify the message size and make it a distribution message. any machine which receives a distribution executes it locally.
     *
     * @param operationRequestRequest
     * @return
     */

    private byte[] createNetworkPayload(OperationRequest operationRequestRequest) {
        OperationRequest distributionRequest =
                OperationRequest.newBuilder(operationRequestRequest).setOperationRequestType(OperationRequestType.DISTRIBUTION).build();
        byte[] senderFrame = ByteManipulationUtils.createFrame(distributionRequest);
        return senderFrame;
    }

    private OperationResponse getResponseForAlter(OperationRequest operationRequest) throws IllegalAccessException, IOException {
        String key = operationRequest.getKey();
        Optional<String> primaryNodeForKey = nodeManager.getNodeAddressForKey(key);
        if (primaryNodeForKey.isEmpty()) {
            logger.info("Entry is being added for the first time request ={}", operationRequest);
            nodeManager.populateKeyNodeMapping(Sets.newHashSet(Arrays.asList(key)));
            primaryNodeForKey = nodeManager.getNodeAddressForKey(key);
        }
        //First Execute on Local - If success then replicate on other servers as a Distribution Request - as it always executes on local.
        //Executing on local
        logger.info("Primary node for key: {} will be: {}", key, primaryNodeForKey);
        if (nodeManager.getCurrentNodeAddress().equals(primaryNodeForKey.get())) {
            OperationResponse localResponse = process(operationRequest);
            // Now process on Remote
            executeOnCluster(operationRequest);
            return localResponse;
        } else {
            return sendRequestToNode(createNetworkPayload(operationRequest), primaryNodeForKey.get());

        }
    }

    private OperationResponse getResponseForFetchAndExist(OperationRequest operationRequest) throws IOException, IllegalAccessException {
        Optional<String> primaryNodeForKey = nodeManager.getNodeAddressForKey(operationRequest.getKey());
        if (primaryNodeForKey.isEmpty()) {
            throw new IllegalAccessException(String.format("No record exists for key = %s  in request =%s ", operationRequest.getKey(), operationRequest));

        }
        if (nodeManager.getCurrentNodeAddress().equals(primaryNodeForKey.get())) {
            logger.info("LOCAL getResponseForFetchAndExist  OperationRequest {} " +
                    "currentNode {}  primaryNodeKey {}", operationRequest, nodeManager.getCurrentNodeAddress(), primaryNodeForKey);
            return process(operationRequest);
        }
        logger.info("REMOTE getResponseForFetchAndExist OperationRequest {} " +
                "currentNode {}  primaryNodeKey {}", operationRequest, nodeManager.getCurrentNodeAddress(), primaryNodeForKey);
        return sendRequestToNode(createNetworkPayload(operationRequest), primaryNodeForKey.get());

    }

    private OperationResponse getResponseForClear(OperationRequest operationRequest) throws IllegalAccessException, IOException {
        executeOnCluster(operationRequest);
        return process(operationRequest);
    }

    private OperationResponse getResponseForDistribution(OperationRequest operationRequest) throws IllegalAccessException {
        return process(operationRequest);

    }

    private void validateOperationRequest(OperationRequest operationRequest) {

        Validate.isTrue(operationRequest != null, "operationRequest cannot be null");
        OperationType operationType = OperationType.valueOf(operationRequest.getOperationRequestType().name());
        boolean isKeyPresent = operationRequest.hasKey();
        boolean isValuePresent = operationRequest.hasValues();

        boolean violation = false;
        switch (operationType) {
            case EXISTS:
            case FETCH:
            case ALTER: {
                violation = !isKeyPresent && !isValuePresent;

            }
            case DISTRIBUTION:
            case CLEAR: {
                break;
            }
            default: {
                violation = true;
            }
        }
        if (violation) {
            throw new IllegalArgumentException("Insufficient key / server information to proceed operationRequest = {} " + operationRequest);
        }

    }

    private OperationResponse sendRequestToNode(byte[] buf, String node) throws IOException {
        return ByteManipulationUtils.deSerializeProto(nodeManager.publishToNode(buf, node), OperationResponse.parser());
    }


}
