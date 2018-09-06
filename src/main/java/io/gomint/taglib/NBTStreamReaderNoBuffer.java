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

        byte[] data = new byte[2];
        this.in.read( data );

        if ( this.byteOrder == ByteOrder.BIG_ENDIAN ) {
            return (short) ( ( data[0] << 8 ) | ( data[1] & 0xff ) );
        }

        return (short) ( ( data[1] << 8 ) | ( data[0] & 0xff ) );
    }

    protected int readIntValue() throws IOException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarInt( this );
        }

        this.expectInput( 4, "Invalid NBT Data: Expected int" );
        byte[] data = new byte[4];
        this.in.read( data );

        if ( this.byteOrder == ByteOrder.BIG_ENDIAN ) {
            return ( ( ( data[0] ) << 24 ) |
                    ( ( data[1] & 0xff ) << 16 ) |
                    ( ( data[2] & 0xff ) << 8 ) |
                    ( data[3] & 0xff ) );
        }

        return ( ( ( data[3] ) << 24 ) |
                ( ( data[2] & 0xff ) << 16 ) |
                ( ( data[1] & 0xff ) << 8 ) |
                ( data[0] & 0xff ) );
    }

    protected long readLongValue() throws IOException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarLong( this ).longValue();
        } else {
            this.expectInput( 8, "Invalid NBT Data: Expected long" );
            byte[] data = new byte[8];
            this.in.read( data );

            if ( this.byteOrder == ByteOrder.BIG_ENDIAN ) {
                return ( ( ( (long) data[0] ) << 56 ) |
                        ( ( (long) data[1] & 0xff ) << 48 ) |
                        ( ( (long) data[2] & 0xff ) << 40 ) |
                        ( ( (long) data[3] & 0xff ) << 32 ) |
                        ( ( (long) data[4] & 0xff ) << 24 ) |
                        ( ( (long) data[5] & 0xff ) << 16 ) |
                        ( ( (long) data[6] & 0xff ) << 8 ) |
                        ( (long) data[7] & 0xff ) );
            }

            return ( ( ( (long) data[7] ) << 56 ) |
                    ( ( (long) data[6] & 0xff ) << 48 ) |
                    ( ( (long) data[5] & 0xff ) << 40 ) |
                    ( ( (long) data[4] & 0xff ) << 32 ) |
                    ( ( (long) data[3] & 0xff ) << 24 ) |
                    ( ( (long) data[2] & 0xff ) << 16 ) |
                    ( ( (long) data[1] & 0xff ) << 8 ) |
                    ( (long) data[0] & 0xff ) );
        }
    }

    protected float readFloatValue() throws IOException {
        this.expectInput( 4, "Invalid NBT Data: Expected float" );
        return Float.intBitsToFloat( this.readIntValue() );
    }

    protected double readDoubleValue() throws IOException {
        this.expectInput( 8, "Invalid NBT Data: Expected double" );
        return Double.longBitsToDouble( this.readLongValue() );
    }

    protected byte[] readByteArrayValue() throws IOException {
        int size = this.readIntValue();
        this.expectInput( size, "Invalid NBT Data: Expected byte array data" );
        byte[] data = new byte[size];
        this.in.read( data );
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
