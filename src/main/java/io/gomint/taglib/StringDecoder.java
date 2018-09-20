package io.gomint.taglib;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface StringDecoder {

    String decode( byte[] data, int length, int offset );

}
