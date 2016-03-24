package io.gomint.taglib;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface NBTStreamListener {

    /**
     * Gets invoked when a NBT node has been read. After this invocation the value is discarded.
     *
     * @param path  The NBT Path of this value. For example "Level.xPos"
     * @param value The NBT Value of this path. For example 8
     * @throws Exception An exception which occured while handling this path.
     */
    void onNBTValue( String path, Object value ) throws Exception;

}
