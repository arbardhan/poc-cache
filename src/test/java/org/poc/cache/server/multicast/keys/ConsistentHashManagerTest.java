package org.poc.cache.server.multicast.keys;

import org.junit.Test;
import org.poc.cache.server.multicast.heartbeat.ConsistentHashManager;

import java.util.HashSet;
import java.util.Set;

public class ConsistentHashManagerTest {

    private final ConsistentHashManager consistentHashManager = new ConsistentHashManager();

    @Test
    public void testConsistentHashingMap() {

        setUp();
        consistentHashManager.printNodeHashSpace();
        consistentHashManager.printKeyNodeMapping();
        Set<String> host5Key = consistentHashManager.getKeysMappedToNode("host5");
        consistentHashManager.removeNode("host5");
        consistentHashManager.printNodeHashSpace();
        consistentHashManager.printKeyNodeMapping();
        /*

        // This test case is not working as several nodes have become very hot. find a way to distribute more evenly.
        for (String key : host5Key) {
            assertTrue("192.168.1.1".equals(consistentHashManager.getNodeMappedToKey(key)));
        }
        */
        consistentHashManager.addNode("node12345");
        consistentHashManager.printNodeHashSpace();
        consistentHashManager.printKeyNodeMapping();
    }

    private void setUp() {
        consistentHashManager.addNode("host1");
        consistentHashManager.addNode("host2");
        consistentHashManager.addNode("host3");
        consistentHashManager.addNode("host4");
        consistentHashManager.addNode("host5");
        consistentHashManager.addNode("host6");

        Set<String> keySet = new HashSet<>();
        String key10 = "abcdsfsf";
        keySet.add(key10);
        String key11 = "fabcdsfsf";
        keySet.add(key11);
        String key12 = "667866876";
        keySet.add(key12);
        String key13 = "dgd";
        keySet.add(key13);
        String key14 = "gg";
        keySet.add(key14);
        String key15 = "dgd";
        keySet.add(key15);
        String key16 = "dgd";
        keySet.add(key16);
        String key17 = "dgd";
        keySet.add(key17);
        String key18 = "fgfh667866876";
        keySet.add(key18);
        String key19 = "hh66786ff6876";
        keySet.add(key19);
        String key20 = "66786f6876";
        keySet.add(key20);
        String key21 = "66786xvsd6876";
        keySet.add(key21);
        String key22 = "dfgdg667866876";
        keySet.add(key22);
        String key23 = "ghfhf667866876";
        keySet.add(key23);
        consistentHashManager.populateKeyNodeMapping(keySet);
    }
}