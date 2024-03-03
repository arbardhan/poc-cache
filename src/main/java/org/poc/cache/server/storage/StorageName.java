package org.poc.cache.server.storage;

import java.util.EnumSet;

/*
    The index of the Map decides the position in the Storage Atomic Reference Array
 */
public enum StorageName {

    SAMPLE_MAP(0),

    CLIENT(1),

    TRANSACTION(2);
    private final int index;
    StorageName(int index){
        this.index=index;
    }

    public int getIndex() {
        return index;
    }

    public static StorageName getStorageNameFromIndex(int index) throws IllegalAccessException {
        EnumSet<StorageName> storageNames = EnumSet.allOf(StorageName.class);
        for(StorageName storageName : storageNames){
            if(storageName.index==index){
                return storageName;
            }
        }
        throw new IllegalAccessException("Invalid Enum Index Passed "+index);
    }
}
