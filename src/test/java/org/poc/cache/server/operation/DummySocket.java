package org.poc.cache.server.operation;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Stack;

public class DummySocket extends Socket {

    private final CustomStack bytes;
    private final String host;
    private final int port;

    public DummySocket(final String host, final int port) throws IOException {
        if(StringUtils.isEmpty(host)){
            throw new IOException("Illegal connection parameters");
        }
        if(port <0){
            throw new UnknownHostException(" Host not known ");
        }

        this.host=host;
        this.port=port;
        this.bytes =new CustomStack();
    }

    public InputStream getInputStream(){
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 10;
            }

            @Override
            public int read(byte[] buf) throws IOException {
                byte[] byteSrc = bytes.readNBytes();
                System.arraycopy(byteSrc,0,buf,0,byteSrc.length);
                return byteSrc.length;
            }

            public byte[] readNBytes(int byteCount) throws IOException {
                return bytes.readNBytes();
            }
        };
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                int i=1;
            }
            public void write(byte[] b) throws IOException {
                bytes.write(b);
            }
        };
    }

    private class CustomStack{
        private final Stack<byte[]> customBytes;
        private CustomStack(){
          this.customBytes=new Stack<>();
        }

        public void write(byte[] bytes){
            customBytes.push(bytes);
        }

        public byte[] readNBytes(){
            return customBytes.pop();
        }
        public byte[] read(){
            return customBytes.pop();
        }

    }

}
