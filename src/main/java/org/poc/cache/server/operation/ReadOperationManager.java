package org.poc.cache.server.operation;

import com.google.protobuf.ByteString;
import org.poc.cache.server.proto.OperationRequestOuterClass.OperationRequest;
import org.poc.cache.server.proto.OperationResponseOuterClass.OperationResponse;
import org.poc.cache.server.storage.IStorage;
import org.poc.cache.server.storage.StorageName;
import org.poc.cache.server.utils.OperationRequestResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
public class ReadOperationManager implements IOperationManager {

    private static final Logger logger = LoggerFactory.getLogger(ReadOperationManager.class);
    private final OperationInterceptor operationInterceptor;
    private final IStorage storage;

    @Autowired
    public ReadOperationManager(OperationInterceptor operationInterceptor, IStorage storage) {
        this.operationInterceptor = operationInterceptor;
        this.storage = storage;
    }

    @PostConstruct
    public void registerInterceptor() {
        operationInterceptor.addOperationManager(this);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.FETCH;
    }

    @Override
    public OperationResponse process(OperationRequest operationRequest) throws IllegalAccessException {
        try {
            StorageName storageNameFromIndex = StorageName.getStorageNameFromIndex(operationRequest.getStorageName());
            ByteString readResponse = ByteString.copyFrom(storage.get(storageNameFromIndex, operationRequest.getKey()));

            OperationResponse successResponse = OperationRequestResponseUtils.getSuccessResponse(operationRequest, Optional.of(readResponse));
            logger.info("Yes we altered {} ",successResponse);
            return successResponse;

        } catch (Exception e) {

            logger.error("Exception occurred in Read for request = {} Exception = {}", operationRequest, e);
            return OperationRequestResponseUtils.getFailureResponse(operationRequest);
        }

    }
}
