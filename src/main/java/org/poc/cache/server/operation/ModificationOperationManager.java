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
public class ModificationOperationManager implements IOperationManager {

    private static final Logger logger = LoggerFactory.getLogger(ModificationOperationManager.class);

    private final OperationInterceptor operationInterceptor;
    private final IStorage storage;

    @Autowired
    public ModificationOperationManager(OperationInterceptor operationInterceptor, IStorage storage) {
        this.operationInterceptor = operationInterceptor;
        this.storage = storage;
        operationInterceptor.addOperationManager(this);
    }

    @PostConstruct
    public void registerInterceptor() {
        operationInterceptor.addOperationManager(this);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.ALTER;
    }

    @Override
    public OperationResponse process(OperationRequest operationRequest)  {

        try {
            StorageName storageNameFromIndex = StorageName.getStorageNameFromIndex(operationRequest.getStorageName());
            storage.put(storageNameFromIndex, operationRequest.getKey(), operationRequest.getValues().toByteArray());
            return OperationRequestResponseUtils.getSuccessResponse(operationRequest, Optional.empty());

        } catch (Exception e) {

            logger.error("Exception occurred in Modification for request = {} Exception = {}", operationRequest, e);
            return OperationRequestResponseUtils.getFailureResponse(operationRequest);
        }
    }
}
