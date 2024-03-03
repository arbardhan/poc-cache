package org.poc.cache.server.operation;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.poc.cache.server.DummySocketConnectionManager;
import org.poc.cache.server.NodeManager;
import org.poc.cache.server.multicast.MulticastCommQueue;
import org.poc.cache.server.multicast.heartbeat.ConsistentHashManager;
import org.poc.cache.server.proto.OperationRequestOuterClass.OperationRequest;
import org.poc.cache.server.proto.OperationRequestTypeOuterClass.OperationRequestType;
import org.poc.cache.server.proto.OperationResponseOuterClass;
import org.poc.cache.server.storage.StorageName;
import org.poc.cache.server.storage.StorageUnit;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = {EnvProperties.class, ConsistentHashManager.class, MulticastCommQueue.class,
        DummySocketConnectionManager.class,StorageUnit.class,NodeManager.class})
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application-test.properties")

@Ignore
public class OperationInterceptorTest {

    private static final Logger logger = LoggerFactory.getLogger(OperationInterceptorTest.class);

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    MulticastCommQueue multicastCommQueue;

    @Autowired
    ConsistentHashManager consistentHashManager;
    @Autowired
    private DummySocketConnectionManager dummySocketConnectionManager;

    @Autowired
    private StorageUnit storageUnit;

    @Autowired
    private NodeManager nodeManager;

    private OperationInterceptor operationInterceptor;

    @Before
    public void setUp() throws InterruptedException {
        setUpTestInstance();
    }


    @Test
    public void testIncomingRequestHandling()  {
        Exception thrownException = assertThrows(IllegalArgumentException.class, () -> operationInterceptor.handleIncomingRequest(null));
        assertTrue(thrownException.getMessage().equals("operationRequest cannot be null"));
    }

    // This will provded a partially built response as they dummy socket just returns what we send
    @Test
    public void testOperationRequest() throws IOException, IllegalAccessException, InterruptedException {

        Thread.sleep(1000);
        OperationRequest operationRequestCreate = createSampleOperationRequest(OperationType.ALTER, StorageName.SAMPLE_MAP, "key1", "data1");
        OperationResponseOuterClass.OperationResponse operationResponseKey1 = operationInterceptor.handleIncomingRequest(operationRequestCreate);

    }

    private void setUpTestInstance() throws InterruptedException {

        if(null != this.operationInterceptor) {
            logger.info("Skipped setup"); return;
        }
        logger.info("creating setup");
        MockitoAnnotations.openMocks(this);
        this.nodeManager = new NodeManager(envProperties,consistentHashManager,multicastCommQueue,dummySocketConnectionManager);
        this.operationInterceptor = new OperationInterceptor(nodeManager,envProperties);
        operationInterceptor.addOperationManager(new ModificationOperationManager(this.operationInterceptor,storageUnit));
        operationInterceptor.addOperationManager(new ReadOperationManager(this.operationInterceptor,storageUnit));
        operationInterceptor.addOperationManager(new ExistsOperationManager(this.operationInterceptor,storageUnit));
        operationInterceptor.addOperationManager(new ClearOperationManager(this.operationInterceptor,storageUnit));
        operationInterceptor.addOperationManager(new DistributionOperationManager(this.operationInterceptor,storageUnit));

        multicastCommQueue.put(Pair.of("node1", MulticastCommQueue.MultiCastQueueEventType.ADD));
        multicastCommQueue.put(Pair.of("node2", MulticastCommQueue.MultiCastQueueEventType.ADD));
        multicastCommQueue.put(Pair.of("node3", MulticastCommQueue.MultiCastQueueEventType.ADD));
    }

    /**
     *  Create sample operations request for a given key value pair and host. here the value is being passed as string -- need to register methods that will
     *  force gpb value types
     *
     */
    private OperationRequest createSampleOperationRequest(OperationType operationType, StorageName storageName, String key , String data){
        logger.info(operationType.name());
        OperationRequestType operationRequestType = OperationRequestType.valueOf(operationType.name());

        OperationRequest.Builder operationRequestBuilder = OperationRequest.newBuilder().setRequestId(operationType.name() + "-" + storageName.name() + "-" + key);
        operationRequestBuilder = StringUtils.isEmpty(key)?operationRequestBuilder:operationRequestBuilder.setKey(key);
        operationRequestBuilder = StringUtils.isEmpty(data)?operationRequestBuilder:operationRequestBuilder.setValues(ByteString.copyFromUtf8(data));
        operationRequestBuilder.setOperationRequestType(operationRequestType);
        OperationRequest operationResponseKey1 = operationRequestBuilder.build();
        logger.info("operationResponseKey1-operationResponseKey1:  {}  reqId: {} ,  values : {}",operationResponseKey1,operationResponseKey1.getRequestId()
                ,operationResponseKey1.getValues());

        return operationResponseKey1;

    }



}
