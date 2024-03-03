package org.poc.cache.server.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of the multicast socket.
 */
public class DummyMulticastSocket extends MulticastSocket {

    Map<String, ArrayBlockingQueue<DatagramPacket>> multicastGroupHolder = new ConcurrentHashMap<>();
    public DummyMulticastSocket(int port) throws IOException {
        super(port);
    }

    public void joinGroup(InetAddress multicastAddress)  {
        String hostAddress = multicastAddress.getHostAddress();
        multicastGroupHolder.computeIfAbsent(hostAddress, k -> new ArrayBlockingQueue<DatagramPacket>(20));
    }

    public void send(DatagramPacket p) throws IOException {
        String hostAddress = p.getAddress().getHostAddress();
        try {
            multicastGroupHolder.get(hostAddress).put(p);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

    }

    public synchronized void receive(DatagramPacket p) throws IOException {
        String hostAddress = p.getAddress().getHostAddress();
        try {
            if(null==multicastGroupHolder.get(hostAddress).peek()){
                throw new IOException("CUSTOM_EXCEPTION_TO_SOCKET");
            }
            DatagramPacket receivedPacket = multicastGroupHolder.get(hostAddress).take();
            p.setData(receivedPacket.getData());
            p.setLength(receivedPacket.getData().length);
        } catch (InterruptedException e) {

        }

    }

}
