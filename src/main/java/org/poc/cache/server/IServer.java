package org.poc.cache.server;

import java.io.IOException;

public interface IServer {
    public void startup(int port) throws IOException;
    public String serveTimeWithOffset(int offSet); // A test endpoint that will server utcTime+ offset back in milliseconds
}
