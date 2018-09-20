package io.gomint.taglib;

/**
 * @author geNAZt
 * @version 1.0
 */
public class FallbackDecoder implements StringDecoder {

    @Override
    public String decode( byte[] data, int length, int offset ) {
        return new String( data, length, offset );
    }

}
