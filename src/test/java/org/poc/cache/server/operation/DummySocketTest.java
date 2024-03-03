package org.poc.cache.server.operation;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DummySocketTest {

    @Test
    public void testDummySocket() throws IOException {
        DummySocket socket = new DummySocket("hst1",1324);
        byte[] bytes = "HELLO-WORLD".getBytes(StandardCharsets.UTF_8);
        socket.getOutputStream().write(bytes);

    }
}
