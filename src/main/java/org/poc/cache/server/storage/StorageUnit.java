package org.poc.cache.server.storage;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class StorageUnit implements IStorage{
    /*
        The key is an interface implementation of Storage Key and the value will be a byte array. The storage key will provide pointers to the type of
        class the storage value may be. Force the implementation of the hashcode and equals method - This way no need to for string key ? No keep it as
        these keys will be multicast and the serialization will be faster.

        for now use the stringiFied storage key as the map key
     */

    private final ConcurrentHashMap<String,byte[]>[] storageArray = new ConcurrentHashMap[12];

    @Override
    public byte[] get(StorageName storageName, String string) {
        if(storageArray[storageName.getIndex()] ==null){
            throw new IllegalArgumentException("No storage exists for  "+storageName);
        }
        return storageArray[storageName.getIndex()].get(string);
    }

    @Override
    public void put(StorageName storageName, String key, byte[] value) {
        storageArray[storageName.getIndex()].put(key,value);
    }

    @Override
    public void clear(StorageName storageName) {
        storageArray[storageName.getIndex()].clear();
    }

    @Override
    public boolean contains(StorageName storageName, String key) {
        return storageArray[storageName.getIndex()].containsKey(key);
    }

    @Override
    public void addNewStorage(StorageName storageName) {
        storageArray[storageName.getIndex()] = new ConcurrentHashMap<String,byte[]>();
    }
}
