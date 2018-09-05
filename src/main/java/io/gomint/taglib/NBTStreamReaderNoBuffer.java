package io.gomint.taglib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author geNAZt
 * @version 1.0
 */
public class NBTStreamReaderNoBuffer {

    protected InputStream in;
    protected ByteOrder byteOrder;

    private boolean useVarint;

    protected NBTStreamReaderNoBuffer( InputStream in, ByteOrder byteOrder ) {
        this.in = in;
        this.byteOrder = byteOrder;
    }

    public void setUseVarint( boolean useVarint ) {
        this.useVarint = useVarint;
    }

    protected byte readByteValue() throws IOException {
        this.expectInput( 1, "Invalid NBT Data: Expected byte" );
        return (byte) this.in.read();
    }

    protected String readStringValue() throws IOException {
        int length = this.useVarint ? VarInt.readUnsignedVarInt( this ) : this.readShortValue();
        this.expectInput( length, "Invalid NBT Data: Expected string bytes" );

        byte[] data = new byte[length];
        this.in.read( data );

        return new String( data, "UTF-8" );
    }

    protected short readShortValue() throws IOException {
        this.expectInput( 2, "Invalid NBT Data: Expected short" );
        byte[] data = new byte[]{this.readByteValue(), this.readByteValue()};
        return ByteBuffer.wrap( data ).order( this.byteOrder ).getShort();
    }

    protected int readIntValue() throws IOException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarInt( this );
        }

        this.expectInput( 4, "Invalid NBT Data: Expected int" );
        byte[] data = new byte[4];
        this.in.read( data );
        return ByteBuffer.wrap( data ).order( this.byteOrder ).getInt();
    }

    protected long readLongValue() throws IOException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarLong( this ).longValue();
        } else {
            this.expectInput( 8, "Invalid NBT Data: Expected long" );
            byte[] data = new byte[8];
            this.in.read( data );
            return ByteBuffer.wrap( data ).order( this.byteOrder ).getLong();
        }
    }

    protected float readFloatValue() throws IOException {
        this.expectInput( 4, "Invalid NBT Data: Expected float" );
        byte[] data = new byte[]{this.readByteValue(), this.readByteValue(), this.readByteValue(), this.readByteValue()};
        return ByteBuffer.wrap( data ).order( this.byteOrder ).getFloat();
    }

    protected double readDoubleValue() throws IOException {
        this.expectInput( 8, "Invalid NBT Data: Expected double" );
        byte[] data = new byte[]{this.readByteValue(), this.readByteValue(), this.readByteValue(), this.readByteValue(),
                this.readByteValue(), this.readByteValue(), this.readByteValue(), this.readByteValue()};
        return ByteBuffer.wrap( data ).order( this.byteOrder ).getDouble();
    }

    protected byte[] readByteArrayValue() throws IOException {
        int size = this.readIntValue();
        this.expectInput( size, "Invalid NBT Data: Expected byte array data" );
        byte[] data = new byte[size];
        for ( int i = 0; i < size; i++ ) {
            data[i] = this.readByteValue();
        }
        return data;
    }

    protected int[] readIntArrayValue() throws IOException {
        int size = this.readIntValue();
        this.expectInput( size * 4, "Invalid NBT Data: Expected int array data" );
        int[] result = new int[size];
        for ( int i = 0; i < size; ++i ) {
            result[i] = this.readIntValue();
        }
        return result;
    }

    protected void expectInput( int remaining, String message ) throws IOException {
        if ( this.in.available() < remaining ) {
            throw new IOException( message );
        }
    }

}
