package org.poc.cache.server.utils;

import org.apache.commons.codec.digest.MurmurHash3;

public class HashUtils {

    public static int getMurmurHash(byte [] input){
        return MurmurHash3.hash32(input) % 1024*1024;
    }

}
