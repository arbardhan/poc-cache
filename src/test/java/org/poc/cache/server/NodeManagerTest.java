package org.poc.cache.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.poc.cache.server.multicast.MulticastCommQueue;
import org.poc.cache.server.multicast.heartbeat.ConsistentHashManager;
import org.poc.cache.server.utils.EnvProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ContextConfiguration(classes = {EnvProperties.class, ConsistentHashManager.class, MulticastCommQueue.class, DummySocketConnectionManager.class})
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-test.properties")
public class NodeManagerTest {

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    MulticastCommQueue multicastCommQueue;

    @Autowired
    ConsistentHashManager consistentHashManager;
    @Autowired
    private DummySocketConnectionManager dummySocketConnectionManager;

    @InjectMocks
    private NodeManager nodeManager;

    @Before
    public void setUp()  {
       MockitoAnnotations.openMocks(this);
       nodeManager = new NodeManager(envProperties,consistentHashManager,multicastCommQueue,dummySocketConnectionManager);
    }

    @Test
    public void createSocketsAndTest() throws IOException {

        dummySocketConnectionManager.addConnectionForCluster("host1");
        dummySocketConnectionManager.addConnectionForCluster("host2");
        nodeManager.printSocketCollection();
        String helloHost1 = "HELLO-HOST1";
        String helloHost2 = "HELLO-HOST2";

        nodeManager.publishToCluster(helloHost1.getBytes(StandardCharsets.UTF_8));
        nodeManager.publishToCluster(helloHost2.getBytes(StandardCharsets.UTF_8));
    }
}
