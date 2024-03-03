package org.poc.cache.server.multicast.heartbeat;

import org.poc.cache.server.utils.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConsistentHashManager {

    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashManager.class);
    private final TreeMap<Integer, String> consistentHashMap = new TreeMap<>();
    private final Map<String, Set<String>> keyNodeMapping = new ConcurrentHashMap<>();

    /**
     * This is called when inserting into the map or removing form the map -- also during distribution for insert or remove.     *
     *
     * @param keys
     */
    public void populateKeyNodeMapping(Set<String> keys) {

        for (String key : keys) {
            String node = calculateNodeForKey(key);
            keyNodeMapping.merge(node, new HashSet<>(Arrays.asList(key)), (oldSet, newSet) -> {
                if (oldSet == null) return newSet;
                oldSet.addAll(newSet);
                return oldSet;
            });
        }
    }

    public Optional<String> getNodeMappedToKey(String key) {

        for (Map.Entry<String, Set<String>> record : keyNodeMapping.entrySet()) {
            if (record.getValue().contains(key)) {
                return Optional.of(record.getKey());
            }
        }
        logger.info("getNodeMappedToKey node not found for key {} ",key);
        return Optional.empty();
    }

    public Set<String> getKeysMappedToNode(String key) {
        return keyNodeMapping.get(key);
    }

    public int addNode(String node) {

        int hash = HashUtils.getMurmurHash(getBytes(node));
        consistentHashMap.put(hash, node);
        logger.info("Added Node {} to map with hashKey {}", node, hash);
        remapKeysToNodes(node);
        return hash;
    }

    public void removeNode(String node) {

        int hash = HashUtils.getMurmurHash(getBytes(node));
        consistentHashMap.remove(hash);
        logger.info("Removed Node {} from map with hashKey {}", node, hash);
        remapKeysToNodes(node);
    }

    // Call this method after a node is added /removed with the ID of the changed node;
    private void remapKeysToNodes(String nodeBeingRemapped) {

        Set<String> keysInNodeBeingRemapped = keyNodeMapping.get(nodeBeingRemapped);
        if (CollectionUtils.isEmpty(keysInNodeBeingRemapped)) return;
        keyNodeMapping.remove(nodeBeingRemapped);
        populateKeyNodeMapping(keysInNodeBeingRemapped);

    }

    private String calculateNodeForKey(String key) {

        printNodeHashSpace();
        int hash = HashUtils.getMurmurHash(getBytes(key));
        SortedMap<Integer, String> tailMap = consistentHashMap.tailMap(hash);
        int positionHash = tailMap.isEmpty() ? consistentHashMap.firstKey() : tailMap.firstKey();
        String node = consistentHashMap.get(positionHash);
        return node;
    }


    private byte[] getBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public String printNodeHashSpace() {
        String ret = "printNodeHashSpace => "+ consistentHashMap.entrySet();
        logger.info(ret);
        return ret;
    }

    public String printKeyNodeMapping() {
        String ret = "printKeyNodeMapping => "+ keyNodeMapping.entrySet();
        logger.info(ret);
        return ret;

    }

}
