package io.gomint.taglib;

import io.gomint.taglib.NBTWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author geNAZt
 * @version 1.0
 */
public class VarInt {

    private static long encodeZigZag32( int v ) {
        return (long) ( v << 1 ^ v >> 31 );
    }

    private static int decodeZigZag32( long v ) {
        return (int) ( v >> 1 ) ^ -( (int) ( v & 1L ) );
    }

    private static void writeUnsignedVarLong( NBTWriter buffer, long value ) throws IOException {
        while ( ( value & -128L ) != 0L ) {
            buffer.writeByteValue( (byte) ( (int) ( value & 127L | 128L ) ) );
            value >>>= 7;
        }

        buffer.writeByteValue( (byte) ( (int) value ) );
    }

    private static long readUnsignedVarLong( NBTStreamReader buffer ) throws IOException {
        long out = 0L;
        int bytes = 0;

        byte in;
        do {
            in = buffer.readByteValue();
            out |= (long) ( ( in & 127 ) << bytes++ * 7 );
            if ( bytes > 7 ) {
                throw new RuntimeException( "VarInt too big" );
            }
        } while ( ( in & 128 ) == 128 );

        return out;
    }

    public static int readSignedVarInt( NBTStreamReader buffer ) throws IOException {
        long val = readUnsignedVarLong( buffer );
        return decodeZigZag32( val );
    }

    public static void writeSignedVarInt( NBTWriter buffer, int value ) throws IOException {
        long signedValue = encodeZigZag32( value );
        writeUnsignedVarLong( buffer, signedValue );
    }

}
