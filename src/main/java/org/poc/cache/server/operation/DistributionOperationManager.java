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
public class DistributionOperationManager implements IOperationManager {

    private static final Logger logger = LoggerFactory.getLogger(DistributionOperationManager.class);

    private final OperationInterceptor operationInterceptor;
    private final IStorage storage;

    @Autowired
    public DistributionOperationManager(OperationInterceptor operationInterceptor, IStorage storage) {
        this.operationInterceptor = operationInterceptor;
        this.storage = storage;
    }

    @PostConstruct
    public void registerInterceptor() {
        operationInterceptor.addOperationManager(this);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.DISTRIBUTION;
    }

    @Override
    public OperationResponse process(OperationRequest operationRequest) throws IllegalAccessException {

        try {

            StorageName storageNameFromIndex = StorageName.getStorageNameFromIndex(operationRequest.getStorageName());
            if (operationRequest.hasKey() && operationRequest.hasValues()) {
                //its a storage distribution request
                logger.info("Running a Put using Distribution operation for request = {}",operationRequest);
                storage.put(storageNameFromIndex, operationRequest.getKey(), operationRequest.getValues().toByteArray());
            }
            if (!operationRequest.hasKey() && !operationRequest.hasValues()) {
                // Its clear Operation
                logger.info("Running a clear using Distribution operation for request = {}",operationRequest);
                storage.clear(storageNameFromIndex);
            }
            return OperationRequestResponseUtils.getSuccessResponse(operationRequest, Optional.empty());

        } catch (Exception e) {

            logger.error("Exception occurred in Distribution for request = {} Exception = {}", operationRequest, e);
            return OperationRequestResponseUtils.getFailureResponse(operationRequest);
        }

    }
}
