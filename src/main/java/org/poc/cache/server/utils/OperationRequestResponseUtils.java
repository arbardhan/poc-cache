package org.poc.cache.server.utils;

import com.google.protobuf.ByteString;
import org.poc.cache.server.proto.OperationRequestOuterClass.OperationRequest;
import org.poc.cache.server.proto.OperationResponseOuterClass.OperationResponse;

import java.util.Optional;

public class OperationRequestResponseUtils {

    public static OperationResponse.Builder getBuilderFromRequest(OperationRequest request) {
        OperationResponse.Builder builder = OperationResponse.newBuilder().setOperationRequestType(request.getOperationRequestType()).setStorageName(request.getStorageName());
        builder = request.hasKey() ? builder.setKey(request.getKey()) : builder;
        return builder;
    }

    public static OperationResponse getSuccessResponse(OperationRequest request, Optional<ByteString> responseByteString) {
        OperationResponse.Builder builder = getBuilderFromRequest(request).setBoolResponse(true);
        builder = responseByteString.isPresent() ? builder.setResponse(responseByteString.get()) : builder;
        return builder.build();
    }

    public static OperationResponse getFailureResponse(OperationRequest request) {
        return getBuilderFromRequest(request).setBoolResponse(false).build();
    }
}
