package io.gomint.taglib;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface NBTStreamListener {

    /**
     * Gets invoked when a NBT node has been read. After this invokation the value is discarded.
     *
     * @param path  The NBT Path of this value. For example "Level.xPos"
     * @param value The NBT Value of this path. For example 8
     */
    void onNBTValue( String path, Object value );

}
