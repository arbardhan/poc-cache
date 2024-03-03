package org.poc.cache.server.operation;

public enum OperationType {
    FETCH, // ALL SIMPLE GET REQUESTS , COUNT ETC
    ALTER, // ALL REQUETS THAT MODIFY THE MAP - PUT , CLEAR
    CLEAR, // WILL CLEAR THE MAP
    EXISTS, // WILL CHECK IF KEY EXISTS IN A MAP
    DISTRIBUTION; // WHEN DATA IS MOVED ACROSS PARTITIONS

    public OperationType getOperationType(String operationType){
        return OperationType.valueOf(operationType);
    }
}
