package org.poc.cache.server;

import com.google.protobuf.ByteString;
import org.poc.cache.server.proto.OperationRequestOuterClass.OperationRequest;
import org.poc.cache.server.proto.OperationRequestTypeOuterClass.OperationRequestType;
import org.poc.cache.server.proto.OperationResponseOuterClass;
import org.poc.cache.server.proto.OperationResponseOuterClass.OperationResponse;
import org.poc.cache.server.storage.IStorage;
import org.poc.cache.server.storage.StorageName;
import org.poc.cache.server.utils.ByteManipulationUtils;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ManagedResource
@Component
public class SampleClient {

    private static final Logger logger = LoggerFactory.getLogger(SampleClient.class);
    private final IStorage storage;
    private final EnvProperties envProperties;
    private final NodeManager nodeManager;

    public SampleClient(final EnvProperties envProperties,final IStorage storage,final NodeManager nodeManager){
        this.envProperties=envProperties;
        this.storage=storage;
        this.nodeManager = nodeManager;
        storage.addNewStorage(StorageName.SAMPLE_MAP);
        storage.addNewStorage(StorageName.CLIENT);
    }

    @ManagedOperation
    public String modify(String key, String value,int storageIndex) throws IOException {

        OperationRequest modify = getBuilder("modify", storageIndex,OperationRequestType.ALTER)
                .setKey(key)
                .setValues(ByteString.copyFrom(value.getBytes(StandardCharsets.UTF_8))).build();
        OperationResponse publish = publish(modify);
        return publish.toString();


    }

    @ManagedOperation
    public String exists(String key,int storageIndex) throws IOException {

        OperationRequest exists = getBuilder("exists", storageIndex,OperationRequestType.EXISTS)
                .setKey(key).build();
        return publish(exists).toString();
    }

    @ManagedOperation
    public String delete(String key,int storageIndex) throws IOException {

        OperationRequest delete = getBuilder("delete", storageIndex,OperationRequestType.CLEAR)
                .setKey(key).build();
        return publish(delete).toString();
    }

    @ManagedOperation
    public String get(String key,int storageIndex) throws IOException {

        OperationRequest get = getBuilder("get", storageIndex,OperationRequestType.FETCH)
                .setKey(key).build();
        logger.info("Sending this request {}",get);
        return publish(get).toString();

    }

    OperationRequest.Builder getBuilder(String source, int storageNumber, OperationRequestType operationRequestType) {
        OperationRequest.Builder builder = OperationRequest.newBuilder()
                .setRequestId("RequestId: "+source).setStorageName(storageNumber).setOperationRequestType(operationRequestType);
        return builder;
    }

    public OperationResponse publish(OperationRequest request) throws IOException {

        byte[] senderFrame = ByteManipulationUtils.createFrame(request);
        OperationResponse operationResponse = ByteManipulationUtils
                .deSerializeProto(nodeManager.publishToNode(senderFrame, envProperties.getLocalNodeIp())
                        , OperationResponseOuterClass.OperationResponse.parser());
        logger.info(" Response for Request: {} is \n {} ",request,operationResponse);
        return operationResponse;
    }


}
