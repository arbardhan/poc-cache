package org.poc.cache.server.operation;

import org.poc.cache.server.proto.OperationRequestOuterClass;
import org.poc.cache.server.proto.OperationResponseOuterClass;

public interface IOperationManager {
    OperationType getOperationType();

    OperationResponseOuterClass.OperationResponse process(OperationRequestOuterClass.OperationRequest operationRequest) throws IllegalAccessException;
}
