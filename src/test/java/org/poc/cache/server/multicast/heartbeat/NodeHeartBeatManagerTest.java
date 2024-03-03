package org.poc.cache.server.multicast.heartbeat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.poc.cache.server.multicast.DummyMulticastSocket;
import org.poc.cache.server.multicast.MulticastCommQueue;
import org.poc.cache.server.proto.NodeHeartBeatOuterClass.NodeHeartBeat;
import org.poc.cache.server.utils.EnvProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = {EnvProperties.class, ConsistentHashManager.class, MulticastCommQueue.class})
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-test.properties")
public class NodeHeartBeatManagerTest {
    @Autowired
    EnvProperties envProperties;
    @Autowired
    ConsistentHashManager consistentHashManager;
    @Autowired
    MulticastCommQueue multicastCommQueue;
    private NodeHeartBeatManager nodeHeartBeatManager;

    @Before
    public void setUp() throws IOException {

        DummyMulticastSocket dummyMulticastSocket = new DummyMulticastSocket(envProperties.getHeartBeatMulticastPort());
        dummyMulticastSocket.joinGroup(InetAddress.getByName(envProperties.getHeartBeatMulticastGroup()));
        nodeHeartBeatManager = new NodeHeartBeatManager(envProperties, consistentHashManager, multicastCommQueue);
        ReflectionTestUtils.setField(nodeHeartBeatManager, "multicastSocket", dummyMulticastSocket);
        ReflectionTestUtils.setField(envProperties, "localNodeIp", "host1");

    }

    @Test
    public void testPublication() throws IOException, InterruptedException {

        nodeHeartBeatManager.publishHeartBeat(2);
        ReflectionTestUtils.setField(envProperties, "localNodeIp", "host2");
        nodeHeartBeatManager.publishHeartBeat(2);
        ReflectionTestUtils.setField(envProperties, "localNodeIp", "host1");
        nodeHeartBeatManager.publishHeartBeat(2);


        try {
            nodeHeartBeatManager.updateHeartBeatFromMulticast();
        } catch (IOException ex) {
            if (!ex.getMessage().equals("CUSTOM_EXCEPTION_TO_SOCKET")) {
                throw ex;
            }

        }

        ReflectionTestUtils.setField(envProperties, "localNodeIp", "host2");
        nodeHeartBeatManager.publishHeartBeat(2);
        try {
            nodeHeartBeatManager.updateHeartBeatFromMulticast();
        } catch (IOException ex) {
            if (!ex.getMessage().equals("CUSTOM_EXCEPTION_TO_SOCKET")) {
                throw ex;
            }

        }

        assertTrue(nodeHeartBeatManager.getNodeHeartBeatMap().size() == 2);
        assertTrue(nodeHeartBeatManager.getNodeHeartBeatMap().keySet().containsAll(new HashSet<>(Arrays.asList("host1", "host2"))));
        long host1StartTime = nodeHeartBeatManager.getNodeHeartBeatMap().get("host1").getHeartBeatStartTime();
        host1StartTime++;
        ReflectionTestUtils.setField(envProperties, "localNodeIp", "host1");
        ReflectionTestUtils.setField(envProperties, "nodeStartUpTimeUTC", host1StartTime);
        AtomicInteger atomicInteger = new AtomicInteger(1);
        ReflectionTestUtils.setField(nodeHeartBeatManager, "nodeHeartBeatSequence", atomicInteger);

        nodeHeartBeatManager.publishHeartBeat(2);
        try {
            nodeHeartBeatManager.updateHeartBeatFromMulticast();
        } catch (IOException ex) {
            if (!ex.getMessage().equals("CUSTOM_EXCEPTION_TO_SOCKET")) {
                throw ex;
            }

        }

        assertTrue(nodeHeartBeatManager.getNodeHeartBeatMap().size() == 2);
        for (NodeHeartBeat beat : nodeHeartBeatManager.getNodeHeartBeatMap().values()) {
            assertTrue(beat.getLiveNodesList().containsAll(new HashSet<>(Arrays.asList("host1", "host2"))));
        }
        Thread.sleep(11000);
        assertTrue(nodeHeartBeatManager.getNodeHeartBeatMap().isEmpty());
        multicastCommQueue.drainAndPrintQueue();
    }
}
