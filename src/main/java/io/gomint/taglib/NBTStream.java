package io.gomint.taglib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * @author geNAZt
 * @version 1.0
 */
public class NBTStream extends NBTStreamReader {

    private enum StreamState {
        INIT,
        PARSING,
    }

    private StreamState state;
    private NBTStreamListener nbtStreamListener;

    public NBTStream( InputStream in, ByteOrder byteOrder ) {
        super( in, byteOrder );
        this.state = StreamState.INIT;
    }

    public void addListener( NBTStreamListener listener ) {
        if ( state == StreamState.INIT ) {
            this.nbtStreamListener = listener;
        }
    }

    public void parse() throws IOException {
        state = StreamState.PARSING;

        if ( this.nbtStreamListener == null ) {
            return;
        }

        this.fetchInput( "Invalid NBT Data: No data at all" );
        if ( this.buffer.remaining() < 3 || this.buffer.get() != NBTDefinitions.TAG_COMPOUND ) {
            throw new IOException( "Invalid NBT Data: No root tag found" );
        }

        // Start reading the compound
        this.readTagCompoundValue( this.readStringValue() );
    }

    private void readTagCompoundValue( String path ) throws IOException {
        this.expectInput( 1, "Invalid NBT Data: Expected Tag ID in compound tag" );
        byte tagID = this.buffer.get();
        path += "." + this.readStringValue();

        while ( tagID != NBTDefinitions.TAG_END ) {
            switch ( tagID ) {
                case NBTDefinitions.TAG_BYTE:
                    this.nbtStreamListener.onNBTValue( path, this.readByteValue() );
                    break;
                case NBTDefinitions.TAG_SHORT:
                    this.nbtStreamListener.onNBTValue( path, this.readShortValue() );
                    break;
                case NBTDefinitions.TAG_INT:
                    this.nbtStreamListener.onNBTValue( path, this.readIntValue() );
                    break;
                case NBTDefinitions.TAG_LONG:
                    this.nbtStreamListener.onNBTValue( path, this.readLongValue() );
                    break;
                case NBTDefinitions.TAG_FLOAT:
                    this.nbtStreamListener.onNBTValue( path, this.readFloatValue() );
                    break;
                case NBTDefinitions.TAG_DOUBLE:
                    this.nbtStreamListener.onNBTValue( path, this.readDoubleValue() );
                    break;
                case NBTDefinitions.TAG_BYTE_ARRAY:
                    this.nbtStreamListener.onNBTValue( path, this.readByteArrayValue() );
                    break;
                case NBTDefinitions.TAG_STRING:
                    this.nbtStreamListener.onNBTValue( path, this.readStringValue() );
                    break;
                case NBTDefinitions.TAG_LIST:
                    this.readTagListValue( path );
                    break;
                case NBTDefinitions.TAG_COMPOUND:
                    this.readTagCompoundValue( path );
                    break;
                case NBTDefinitions.TAG_INT_ARRAY:
                    this.nbtStreamListener.onNBTValue( path, this.readIntArrayValue() );
                    break;
                default:
                    throw new IOException( "Invalid NBT Data: Unknown tag <" + tagID + ">" );
            }

            this.expectInput( 1, "Invalid NBT Data: Expected tag ID in tag compound" );
            tagID = this.buffer.get();
        }
    }

    private void readTagListValue( String path ) throws IOException {
        this.expectInput( 5, "Invalid NBT Data: Expected TAGList header" );
        byte listType = this.buffer.get();
        int listLength = this.readIntValue();

        switch( listType ) {
            case NBTDefinitions.TAG_END:
                // Not to be unseen! Seemingly Mojang cares about something after all: disk space
                break;
            case NBTDefinitions.TAG_BYTE:
                this.expectInput( listLength, "Invalid NBT Data: Expected bytes for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readByteValue() );
                }

                break;
            case NBTDefinitions.TAG_SHORT:
                this.expectInput( 2 * listLength, "Invalid NBT Data: Expected shorts for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readShortValue() );
                }

                break;
            case NBTDefinitions.TAG_INT:
                this.expectInput( 4 * listLength, "Invalid NBT Data: Expected ints for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readIntValue() );
                }

                break;
            case NBTDefinitions.TAG_LONG:
                this.expectInput( 8 * listLength, "Invalid NBT Data: Expected longs for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readLongValue() );
                }

                break;
            case NBTDefinitions.TAG_FLOAT:
                this.expectInput( 4 * listLength, "Invalid NBT Data: Expected floats for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readFloatValue() );
                }

                break;
            case NBTDefinitions.TAG_DOUBLE:
                this.expectInput( 8 * listLength, "Invalid NBT Data: Expected doubles for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readDoubleValue() );
                }

                break;
            case NBTDefinitions.TAG_BYTE_ARRAY:
                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readByteArrayValue() );
                }

                break;
            case NBTDefinitions.TAG_STRING:
                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readStringValue() );
                }

                break;
            case NBTDefinitions.TAG_LIST:
                for ( int i = 0; i < listLength; ++i ) {
                    this.readTagListValue( path + "." + String.valueOf( i ) );
                }

                break;
            case NBTDefinitions.TAG_COMPOUND:
                for ( int i = 0; i < listLength; ++i ) {
                    this.readTagCompoundValue( path + "." + String.valueOf( i ) );
                }

                break;
            case NBTDefinitions.TAG_INT_ARRAY:
                for ( int i = 0; i < listLength; ++i ) {
                    this.nbtStreamListener.onNBTValue( path + "." + String.valueOf( i ), this.readIntArrayValue() );
                }

                break;
            default:
                throw new IOException( "Invalid NBT Data: Unknown tag <" + listType + ">" );
        }
    }

}
