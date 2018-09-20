package io.gomint.taglib;

import sun.nio.cs.ArrayDecoder;

import java.nio.charset.StandardCharsets;

/**
 * @author geNAZt
 * @version 1.0
 */
public class SunDirectDecoder implements StringDecoder {

    private static final ThreadLocal<ArrayDecoder> DECODER = new ThreadLocal<>();

    @Override
    public String decode( byte[] data, int length, int offset ) {
        ArrayDecoder decoder = DECODER.get();
        if ( decoder == null ) {
            decoder = (ArrayDecoder) StandardCharsets.UTF_8.newDecoder();
            DECODER.set( decoder );
        }

        char[] charTemp = new char[data.length];
        int decodeLength = decoder.decode( data, length, offset, charTemp );
        return new String( charTemp, 0, decodeLength );
    }

}
