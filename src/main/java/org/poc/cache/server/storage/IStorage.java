package org.poc.cache.server.storage;

/*
Implements the Basic Memory Storage Unit
    class Storage
      private  AtomicReference<Array[concurrentHashMap]> partitionStorageArray
      public  (byteArray) Object  get(MapName,Key)
      public void put(MapName,Key m ByteArray)
      public boolean addNewPartitionToStorage(enum MapName)
     END Class

There may be several Maps with separate names - they will be declared in advance in an ENUM MapName → ClientCache(CC,0),   LocationCache(LC ,1)
The Enum Index will be used to  create /recover the HashMap of the Cache so CC will be in partitionStorageArray[0] and so on . Hence, to retrive a Key A from CC
        partitionStorageUnit{ClientCache.Index).get("A")
            class Partition
                private  int id
                private Storage
            end class
 */
public interface IStorage {
    byte[] get(StorageName storageName, String key);
    void put(StorageName storageName, String key , byte[] value);
    void clear(StorageName storageName);
    boolean contains(StorageName storageName, String key);
    void addNewStorage(StorageName storageName);

}
