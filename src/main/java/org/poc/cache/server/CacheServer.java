package org.poc.cache.server;

import org.poc.cache.server.operation.OperationExecutor;
import org.poc.cache.server.operation.OperationInterceptor;
import org.poc.cache.server.utils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * If a request is destined to go to an OperationInterceptor , it will go through this. Hence all operation on the cache including
 * partition balancing will happen through this class.
 * </p>
 * <br>
 * <p>
 * Cache Server Components
 * <br>
 * <ol type= "1.">
 * <li> A Server socket running to accept connections on port 1992
 * <li> A native thread pool of constant size 10 where the connection will be dumped
 * <li> A remote thread pool (size 10) that is "actually" a client to connect to a different "CacheServer" thread that
 * which will be activated if the key being requested is not primary to the server being connected to.
 * (which in turn will call native pool of that machine as in operation 2)
 * <li> A collection that contains key ,machine mapping which is needed to perform operation 3.
 * <li> A partition thread pool to handle partition operations of size 5 which can infact re-use remote pool of operation 3 as well.
 * <li> It's the responsibility of the connection object to ensure the entries in the key - machine map are being updated always.
 * </ol>
 *
 * <p>
 * Cache Server Operation Flow for supported operation types
 * </p>
 *
 * <ol type= "I">
 * <li> FETCH
 * <ul>
 * <li>Machine A receives key and Cache Server Key - Machine determines which machine is parent for the key
 * <li>If Its A, server puts the request of the native thread pool
 * <li> The operation interceptor is called and it immediately queries the native map and write result to output stream'
 * <li>If its A separate machine say B, the request is put on the remote thread pool
 * <li> A client on Machine A sends this request over tcp to cache server of "B" awaiting a response in its out put stream.
 * <li> If key is not found in key-machine map a failed response is sent back
 * </ul>
 * <li> ALTER
 * <ul>
 * <li>Machine A receives key and Cache Server Key - Machine determines which machine is parent for the key.
 * <li> If key is not found its a new entry and its hash calc determines machine name.
 * <li>If Its A, server puts the request of the native thread pool and its subsequently put / updated
 * <li> Once operation is complete , a success is returned , The same request is now changed to a distribution request and fired to
 * all server lists except native A using the partition thread.
 * </ul>
 * <li> CLEAR
 * <ul>
 * <li>Clear Request is fired once natively and once for every machine using the remote thread.
 * <li> Results of both the operations are tallied and a final success or failure is returned.
 * </ul>
 * <li> EXISTS
 * <ul>
 * <li>IsExist is fired on the machine using native or remote thread pool.
 * <li> We do not enquire about the key in other partitions , except the machine where it is primary
 * </ul>
 * <li> DISTRIBUTION
 * <ul>
 * <li> This request is fired to distribute partition movement operations during restarts/ loss of member etc.
 * <li> A distribution request cannot be forwarded. It is sent by the source of truth machine and every machine
 * that receives it will need to apply it locally.
 * </ul>
 * </ol>
 *
 * </p>
 * <br>
 * </p>
 */

@Component
public class CacheServer {

    private static final Logger logger = LoggerFactory.getLogger(CacheServer.class);

    private final OperationInterceptor operationInterceptor;
    private final ServerSocket serverSocket;
    private final EnvProperties envProperties;
    private final ExecutorService executorService;

    @Autowired
    public CacheServer(OperationInterceptor operationInterceptor,final EnvProperties envProperties) throws IOException {
        this.operationInterceptor = operationInterceptor;
        this.serverSocket = new ServerSocket(envProperties.getNodeServicePort());
        this.envProperties=envProperties;
        this.executorService = Executors.newFixedThreadPool(envProperties.getNativeThreadPoolSize());
    }

    public void beginServerLoop() throws IOException {
        logger.info("Server starting {} ",serverSocket.getLocalPort());
        while (true) {
            logger.info("Accepting a connection");
            Socket clientSocket = serverSocket.accept();
            logger.info("Accepted a connection");

            try {
                OperationExecutor operationExecutor = new OperationExecutor(operationInterceptor, clientSocket);
                executorService.execute(operationExecutor);

            }
            catch (Exception e){
                logger.error("error in beginServerLoop {}",e);

            }
        }
    }

}
