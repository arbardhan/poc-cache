package org.poc.cache.server.operation;

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
public class ExistsOperationManager implements IOperationManager {

    private static final Logger logger = LoggerFactory.getLogger(ExistsOperationManager.class);

    private final OperationInterceptor operationInterceptor;

    private final IStorage storage;

    @Autowired
    public ExistsOperationManager(OperationInterceptor operationInterceptor, IStorage storage) {
        this.operationInterceptor = operationInterceptor;
        this.storage = storage;
    }

    @PostConstruct
    public void registerInterceptor() {
        operationInterceptor.addOperationManager(this);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.EXISTS;
    }

    @Override
    public OperationResponse process(OperationRequest operationRequest) throws IllegalAccessException {
        try {

            StorageName storageName = StorageName.getStorageNameFromIndex(operationRequest.getStorageName());
            boolean contains = storage.contains(storageName, operationRequest.getKey());
            if (contains) {
                return OperationRequestResponseUtils.getSuccessResponse(operationRequest, Optional.empty());
            }
            return OperationRequestResponseUtils.getFailureResponse(operationRequest);

        } catch (Exception e) {

            logger.error("Exception occurred in Exists for request = {} Exception = {}", operationRequest, e);
            return OperationRequestResponseUtils.getFailureResponse(operationRequest);
        }
    }
}
