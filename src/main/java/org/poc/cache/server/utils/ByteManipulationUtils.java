package org.poc.cache.server.utils;

import com.google.protobuf.*;
import org.poc.cache.server.proto.NodeHeartBeatOuterClass;

import java.nio.ByteOrder;
import java.util.Arrays;

public class ByteManipulationUtils {
    /*
        For Little Endian the most significant digits are at the extreme right in  the way we right arrays.
     */

    private static boolean isLittleEndian(){
        return ByteOrder.nativeOrder()==ByteOrder.LITTLE_ENDIAN;
    }

    public static byte[] writeIntToByte(int input){
        byte[] intAsByteArr = new byte[4];
        if(isLittleEndian()){ // LittleEndian
            intAsByteArr[3] = (byte) (input>>>24 & 0xFF); // Most significant digit at 3
            intAsByteArr[2] = (byte) (input>>>16 & 0xFF);
            intAsByteArr[1] = (byte) (input>>>8 & 0xFF);
            intAsByteArr[0] = (byte) (input & 0xFF);
            //            int i = (intAsByteArr[0] & 0xFF) | (intAsByteArr[1] << 8 & 0xFF) | (intAsByteArr[2] << 16 & 0xFF) | (intAsByteArr[3] << 24 & 0xFF);

        } else {
            intAsByteArr[0] = (byte) (input>>>24 & 0xFF);// Most significant digit at 0
            intAsByteArr[1] = (byte) (input>>>16 & 0xFF);
            intAsByteArr[2] = (byte) (input>>>8 & 0xFF);
            intAsByteArr[3] = (byte) (input & 0xFF);
        }
        return intAsByteArr;
    }

    public static int getIntFromByte(byte[] input){
        int msb;
        int msbMinus1;
        int msbMinus2;
        int msbMinus3;
        if(isLittleEndian()){
            msb = (input[3] & 0xFF) << 24;
            msbMinus1 = (input[2] & 0xFF) << 16;
            msbMinus2 = (input[1] & 0xFF) << 8;
            msbMinus3 = (input[0] & 0xFF);
        } else {
            msb = (input[0] & 0xFF) << 24;
            msbMinus1 = (input[1] & 0xFF) << 16;
            msbMinus2 = (input[2] & 0xFF) << 8;
            msbMinus3 = (input[3] & 0xFF);
        }
        return msb | msbMinus1 | msbMinus2 | msbMinus3;
    }

    public static byte[] createFrame(Message message){
        byte[] buf = message.toByteArray();
        int length = buf.length;
        byte[] senderFrame= new byte[length+4];
        byte[] headerBytes = ByteManipulationUtils.writeIntToByte(length);
        for(int i =0 ; i<senderFrame.length;i++){
            if(i<4){
                senderFrame[i]=headerBytes[i];
            } else {
                senderFrame[i]=buf[i-4];
            }
        }
        return senderFrame;
    }

    public static <T extends Message> T deSerializeProto(byte[] buf, Parser<T> parser ) throws InvalidProtocolBufferException {
        int intFromByte = ByteManipulationUtils.getIntFromByte(Arrays.copyOfRange(buf, 0, 4));
        T message = parser.parseFrom(Arrays.copyOfRange(buf, 4, 4+intFromByte));
        return message;
    }
}
