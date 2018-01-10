package io.gomint.taglib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author geNAZt
 * @version 1.0
 */
public class NBTStreamReader {

    private static final int BUFFER_SIZE = 1024 * 16;

    protected InputStream in;
    protected ByteBuffer buffer;
    protected ByteOrder byteOrder;

    private boolean useVarint;

    protected NBTStreamReader( InputStream in, ByteOrder byteOrder ) {
        this.in = in;
        this.byteOrder = byteOrder;

        byte[] arrayBuffer = new byte[BUFFER_SIZE];
        this.buffer = ByteBuffer.wrap( arrayBuffer );
        this.buffer.order( byteOrder );
        this.buffer.limit( 0 );
        this.buffer.position( 0 );
    }

    public void setUseVarint( boolean useVarint ) {
        this.useVarint = useVarint;
    }

    public boolean hasMoreToRead() {
        try {
            return this.buffer.limit() > this.buffer.position() || this.in.available() > 0;
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return false;
    }

    protected byte readByteValue() throws IOException {
        this.expectInput( 1, "Invalid NBT Data: Expected byte" );
        return this.buffer.get();
    }

    protected String readStringValue() throws IOException {
        int length = this.useVarint ? VarInt.readUnsignedVarInt( this ) : this.readShortValue();
        this.expectInput( length, "Invalid NBT Data: Expected string bytes" );
        String result = new String( this.buffer.array(), this.buffer.position(), length, "UTF-8" );
        this.buffer.position( this.buffer.position() + length );
        return result;
    }

    protected short readShortValue() throws IOException {
        this.expectInput( 2, "Invalid NBT Data: Expected short" );
        return this.buffer.getShort();
    }

    protected int readIntValue() throws IOException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarInt( this );
        }

        this.expectInput( 4, "Invalid NBT Data: Expected int" );
        return this.buffer.getInt();
    }

    protected long readLongValue() throws IOException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarLong( this ).longValue();
        } else {
            this.expectInput( 8, "Invalid NBT Data: Expected long" );
            return this.buffer.getLong();
        }
    }

    protected float readFloatValue() throws IOException {
        this.expectInput( 4, "Invalid NBT Data: Expected float" );
        return this.buffer.getFloat();
    }

    protected double readDoubleValue() throws IOException {
        this.expectInput( 8, "Invalid NBT Data: Expected double" );
        return this.buffer.getDouble();
    }

    protected byte[] readByteArrayValue() throws IOException {
        int size = this.readIntValue();
        this.expectInput( size, "Invalid NBT Data: Expected byte array data" );
        byte[] result = new byte[size];
        System.arraycopy( this.buffer.array(), this.buffer.position(), result, 0, size );
        this.buffer.position( this.buffer.position() + size );
        return result;
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
        // Catch the overflow case:
        if ( remaining > this.buffer.array().length ) {
            int capacity = this.buffer.array().length;
            while ( remaining > capacity ) {
                capacity *= 2;
            }

            byte[] newArray = new byte[capacity];
            int length = this.buffer.remaining();
            System.arraycopy( this.buffer.array(), this.buffer.position(), newArray, 0, length );
            this.buffer = ByteBuffer.wrap( newArray );
            this.buffer.order( this.byteOrder );
            this.buffer.limit( length );
            this.buffer.position( 0 );
        }

        if ( this.buffer.remaining() < remaining ) {
            this.fetchInput( message );
            if ( this.buffer.remaining() < remaining ) {
                throw new IOException( message );
            }
        }
    }

    protected void fetchInput( String message ) throws IOException {
        // Got to do some nasty copies here:
        if ( this.buffer.remaining() > 0 ) {
            // No overwrites following http://docs.oracle.com/javase/7/docs/api/java/lang/System.html#arraycopy(java.lang.Object,%20int,%20java.lang.Object,%20int,%20int)
            System.arraycopy( this.buffer.array(), this.buffer.position(), this.buffer.array(), 0, this.buffer.remaining() );
            this.buffer.limit( this.buffer.remaining() );
            this.buffer.position( 0 );

            int read = this.in.read( this.buffer.array(), this.buffer.limit(), this.buffer.capacity() - this.buffer.limit() );
            if ( read == -1 ) {
                throw new IOException( "NBT input ended unexpectedly!", new IOException( message ) );
            }

            // Flip does not really fit here:
            this.buffer.limit( this.buffer.limit() + read );
            this.buffer.position( 0 );
        } else {
            // Speedier variant if applicable (that is the case quite often, as the buffer is a power of two):
            int read = this.in.read( this.buffer.array(), 0, this.buffer.capacity() );
            if ( read == -1 ) {
                throw new IOException( "NBT input ended unexpectedly!", new IOException( message ) );
            }

            // Flip does not really fit here:
            this.buffer.limit( read );
            this.buffer.position( 0 );
        }
    }

}
