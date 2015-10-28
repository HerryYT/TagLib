/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 GoMint, BlackyPaw and geNAZt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.gomint.taglib;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author BlackyPaw
 * @version 1.0
 */
class NBTReader {

	private static final int BUFFER_SIZE = 1024 * 16;

	private InputStream in;
	private ByteBuffer  buffer;

	public NBTReader( InputStream in ) {
		this.in = in;

		byte[] arrayBuffer = new byte[BUFFER_SIZE];
		this.buffer = ByteBuffer.wrap( arrayBuffer );
		this.buffer.limit( 0 );
		this.buffer.position( 0 );
	}

	public NBTTagCompound parse() throws IOException {
		this.fetchInput( "Invalid NBT Data: No data at all" );
		if ( this.buffer.remaining() < 3 || this.buffer.get() != NBTDefinitions.TAG_COMPOUND ) {
			throw new IOException( "Invalid NBT Data: No root tag found" );
		}
		String name = this.readStringValue();
		NBTTagCompound root = this.readTagCompoundValue();
		root.setName( name );
		return root;
	}

	private NBTTagCompound readTagCompoundValue() throws IOException {
		NBTTagCompound compound = new NBTTagCompound();
		this.expectInput( 1, "Invalid NBT Data: Expected Tag ID in compound tag" );
		byte tagID = this.buffer.get();
		while ( tagID != NBTDefinitions.TAG_END ) {
			switch ( tagID ) {
				case NBTDefinitions.TAG_BYTE:
					compound.addValue( this.readStringValue(), this.readByteValue() );
					break;
				case NBTDefinitions.TAG_SHORT:
					compound.addValue( this.readStringValue(), this.readShortValue() );
					break;
				case NBTDefinitions.TAG_INT:
					compound.addValue( this.readStringValue(), this.readIntValue() );
					break;
				case NBTDefinitions.TAG_LONG:
					compound.addValue( this.readStringValue(), this.readLongValue() );
					break;
				case NBTDefinitions.TAG_FLOAT:
					compound.addValue( this.readStringValue(), this.readFloatValue() );
					break;
				case NBTDefinitions.TAG_DOUBLE:
					compound.addValue( this.readStringValue(), this.readDoubleValue() );
					break;
				case NBTDefinitions.TAG_BYTE_ARRAY:
					compound.addValue( this.readStringValue(), this.readByteArrayValue() );
					break;
				case NBTDefinitions.TAG_STRING:
					compound.addValue( this.readStringValue(), this.readStringValue() );
					break;
				case NBTDefinitions.TAG_LIST:
					compound.addValue( this.readStringValue(), this.readTagListValue() );
					break;
				case NBTDefinitions.TAG_COMPOUND:
					String name = this.readStringValue();
					NBTTagCompound child = this.readTagCompoundValue();
					child.setName( name );
					compound.addChild( child );
					break;
				case NBTDefinitions.TAG_INT_ARRAY:
					compound.addValue( this.readStringValue(), this.readIntArrayValue() );
					break;
				default:
					throw new IOException( "Invalid NBT Data: Unknown tag <" + tagID + ">" );
			}

			tagID = this.buffer.get();
		}
		return compound;
	}
	
	private List<Object> readTagListValue() throws IOException {
		this.expectInput( 5, "Invalid NBT Data: Expected TAGList header" );
		byte listType = this.buffer.get();
		int listLength = this.readIntValue();
		List<Object> backingList = new ArrayList<>( listLength );
		ReadMethod reader = null;

		switch( listType ) {
			case NBTDefinitions.TAG_END:
				// Not to be unseen! Seemingly Mojang cares about something after all: disk space
				listLength = 0;
				break;
			case NBTDefinitions.TAG_BYTE:
				this.expectInput( listLength, "Invalid NBT Data: Expected bytes for list" );
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readByteValue(); } };
				break;
			case NBTDefinitions.TAG_SHORT:
				this.expectInput( 2 * listLength, "Invalid NBT Data: Expected shorts for list" );
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readShortValue(); } };
				break;
			case NBTDefinitions.TAG_INT:
				this.expectInput( 4 * listLength, "Invalid NBT Data: Expected ints for list" );
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readIntValue(); } };
				break;
			case NBTDefinitions.TAG_LONG:
				this.expectInput( 8 * listLength, "Invalid NBT Data: Expected longs for list" );
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readLongValue(); } };
				break;
			case NBTDefinitions.TAG_FLOAT:
				this.expectInput( 4 * listLength, "Invalid NBT Data: Expected floats for list" );
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readFloatValue(); } };
				break;
			case NBTDefinitions.TAG_DOUBLE:
				this.expectInput( 8 * listLength, "Invalid NBT Data: Expected doubles for list" );
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readDoubleValue(); } };
				break;
			case NBTDefinitions.TAG_BYTE_ARRAY:
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readByteArrayValue(); } };
				break;
			case NBTDefinitions.TAG_LIST:
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readTagListValue(); } };
				break;
			case NBTDefinitions.TAG_COMPOUND:
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readTagCompoundValue(); } };
				break;
			case NBTDefinitions.TAG_INT_ARRAY:
				reader = new ReadMethod() { @Override public Object read() throws IOException { return NBTReader.this.readIntArrayValue(); } };
				break;
			default:
				throw new IOException( "Invalid NBT Data: Unknown tag <" + listType + ">" );
		}

		for ( int i = 0; i < listLength; ++i ) {
			backingList.add( reader.read() );
		}
		return backingList;
	}

	private byte readByteValue() throws IOException {
		this.expectInput( 1, "Invalid NBT Data: Expected byte" );
		return this.buffer.get();
	}

	private String readStringValue() throws IOException {
		short length = this.readShortValue();
		this.expectInput( length, "Invalid NBT Data: Expected string bytes" );
		String result = new String( this.buffer.array(), this.buffer.position(), length, "UTF-8" );
		this.buffer.position( this.buffer.position() + length );
		return result;
	}

	private short readShortValue() throws IOException {
		this.expectInput( 2, "Invalid NBT Data: Expected short" );
		return (short) ( ( ( this.buffer.get() & 0xFF ) << 8 ) | ( this.buffer.get() & 0xFF ) );
	}

	private int readIntValue() throws IOException {
		this.expectInput( 4, "Invalid NBT Data: Expected int" );
		return (int) ( ( ( this.buffer.get() & 0xFF ) << 24 ) |
		               ( ( this.buffer.get() & 0xFF ) << 16 ) |
		               ( ( this.buffer.get() & 0xFF ) << 8 ) |
		               ( ( this.buffer.get() & 0xFF ) ) );
	}

	private long readLongValue() throws IOException {
		this.expectInput( 8, "Invalid NBT Data: Expected long" );
		return ( ( (long) ( this.buffer.get() & 0xFF ) << 56 ) |
		         ( (long) ( this.buffer.get() & 0xFF ) << 48 ) |
		         ( (long) ( this.buffer.get() & 0xFF ) << 40 ) |
		         ( (long) ( this.buffer.get() & 0xFF ) << 32 ) |
		         ( ( this.buffer.get() & 0xFF ) << 24 ) |
		         ( ( this.buffer.get() & 0xFF ) << 16 ) |
		         ( ( this.buffer.get() & 0xFF ) << 8 ) |
		         ( ( this.buffer.get() & 0xFF ) ) );
	}

	private float readFloatValue() throws IOException {
		this.expectInput( 4, "Invalid NBT Data: Expected float" );
		int i = (int) ( ( ( this.buffer.get() & 0xFF ) << 24 ) |
		                ( ( this.buffer.get() & 0xFF ) << 16 ) |
		                ( ( this.buffer.get() & 0xFF ) << 8 ) |
		                ( ( this.buffer.get() & 0xFF ) ) );
		return Float.intBitsToFloat( i );
	}

	private double readDoubleValue() throws IOException {
		this.expectInput( 8, "Invalid NBT Data: Expected double" );
		long l = ( ( (long) ( this.buffer.get() & 0xFF ) << 56 ) |
		           ( (long) ( this.buffer.get() & 0xFF ) << 48 ) |
		           ( (long) ( this.buffer.get() & 0xFF ) << 40 ) |
		           ( (long) ( this.buffer.get() & 0xFF ) << 32 ) |
		           ( ( this.buffer.get() & 0xFF ) << 24 ) |
		           ( ( this.buffer.get() & 0xFF ) << 16 ) |
		           ( ( this.buffer.get() & 0xFF ) << 8 ) |
		           ( ( this.buffer.get() & 0xFF ) ) );
		return Double.longBitsToDouble( l );
	}

	private byte[] readByteArrayValue() throws IOException {
		int size = this.readIntValue();
		this.expectInput( size, "Invalid NBT Data: Expected byte array data" );
		byte[] result = new byte[size];
		System.arraycopy( this.buffer.array(), this.buffer.position(), result, 0, size );
		this.buffer.position( this.buffer.position() + size );
		return result;
	}

	private int[] readIntArrayValue() throws IOException {
		int size = this.readIntValue();
		this.expectInput( size * 4, "Invalid NBT Data: Expected int array data" );
		int[] result = new int[size];
		for ( int i = 0; i < size; ++i ) {
			result[i] = this.readIntValue();
		}
		return result;
	}

	private void expectInput( int remaining, String message ) throws IOException {
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

	private void fetchInput( String message ) throws IOException {
		// Got to do some nasty copies here:
		if ( this.buffer.remaining() > 0 ) {
			// No overwrites following http://docs.oracle.com/javase/7/docs/api/java/lang/System.html#arraycopy(java.lang.Object,%20int,%20java.lang.Object,%20int,%20int)
			System.arraycopy( this.buffer.array(), this.buffer.position(), this.buffer.array(), 0, this.buffer.remaining() );
			this.buffer.limit( this.buffer.remaining() );
			this.buffer.position( 0 );
		}

		int read = this.in.read( this.buffer.array(), this.buffer.limit(), this.buffer.capacity() - this.buffer.limit() );
		if ( read == -1 ) {
			throw new IOException( "NBT input ended unexpectedly!", new IOException( message ) );
		}

		// Flip does not really fit here:
		this.buffer.limit( this.buffer.limit() + read );
		this.buffer.position( 0 );
	}

}
