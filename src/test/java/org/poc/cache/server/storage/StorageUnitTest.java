package org.poc.cache.server.storage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertTrue;

public class StorageUnitTest {

    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();
    private final StorageUnit storageUnit = new StorageUnit();

    @Test
    public void testGet() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(containsString("No storage exists for  SAMPLE_MAP"));
        storageUnit.get(StorageName.SAMPLE_MAP, "KEY1");
    }

    @Test
    public void testGetPutOperations() {
        storageUnit.addNewStorage(StorageName.SAMPLE_MAP);
        storageUnit.addNewStorage(StorageName.CLIENT);

        storageUnit.put(StorageName.SAMPLE_MAP, "SAMPLE_MAP_KEY1", "SAMPLE_MAP_VALUE1".getBytes(StandardCharsets.UTF_8));
        storageUnit.put(StorageName.SAMPLE_MAP, "SAMPLE_MAP_KEY2", "SAMPLE_MAP_VALUE2".getBytes(StandardCharsets.UTF_8));
        storageUnit.put(StorageName.SAMPLE_MAP, "CLIENT_KEY1", "CLIENT_VALUE1".getBytes(StandardCharsets.UTF_8));
        storageUnit.put(StorageName.SAMPLE_MAP, "CLIENT_KEY2", "CLIENT_VALUE2".getBytes(StandardCharsets.UTF_8));

        String valueForSampleMapKey1 = new String(storageUnit.get(StorageName.SAMPLE_MAP, "SAMPLE_MAP_KEY1"), StandardCharsets.UTF_8);
        String valueForSampleMapKey2 = new String(storageUnit.get(StorageName.SAMPLE_MAP, "SAMPLE_MAP_KEY2"), StandardCharsets.UTF_8);
        String valueForClientKey1 = new String(storageUnit.get(StorageName.SAMPLE_MAP, "CLIENT_KEY1"), StandardCharsets.UTF_8);
        String valueForClientKey2 = new String(storageUnit.get(StorageName.SAMPLE_MAP, "CLIENT_KEY2"), StandardCharsets.UTF_8);

        String concatResult = valueForSampleMapKey1 + " " + valueForSampleMapKey2 + " " + valueForClientKey1 + " " + valueForClientKey2;

        assertTrue(concatResult.equals("SAMPLE_MAP_VALUE1 SAMPLE_MAP_VALUE2 CLIENT_VALUE1 CLIENT_VALUE2"));
    }


    @Test
    public void testContains() {
        storageUnit.addNewStorage(StorageName.SAMPLE_MAP);
        storageUnit.put(StorageName.SAMPLE_MAP, "k1", "SAMPLE_MAP_VALUE1".getBytes(StandardCharsets.UTF_8));

        boolean shouldReturnFalse = storageUnit.contains(StorageName.SAMPLE_MAP, "SAMPLE_MAP_KEY144");
        boolean shouldReturnTrue = storageUnit.contains(StorageName.SAMPLE_MAP, "k1");

        assertTrue(!shouldReturnFalse && shouldReturnTrue);
    }

    @Test
    public void testClear() {

        storageUnit.addNewStorage(StorageName.SAMPLE_MAP);
        storageUnit.put(StorageName.SAMPLE_MAP, "k1", "k1Value".getBytes(StandardCharsets.UTF_8));

        boolean shouldReturnTrue = storageUnit.contains(StorageName.SAMPLE_MAP, "k1");

        storageUnit.clear(StorageName.SAMPLE_MAP);

        boolean shouldReturnFalse = storageUnit.contains(StorageName.SAMPLE_MAP, "k1");

        assertTrue(!shouldReturnFalse && shouldReturnTrue);
    }


}
