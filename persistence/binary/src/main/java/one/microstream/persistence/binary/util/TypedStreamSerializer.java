package one.microstream.persistence.binary.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.XIO;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksWrapper;


public interface TypedStreamSerializer
{
	public void serialize(Object object, OutputStream out);
	
	public <T> T deserialize(InputStream in);
	
	
	
	public static TypedStreamSerializer New()
	{
		return new TypedStreamSerializer.Default(SerializerFoundation.New());
	}
	
	
	public static TypedStreamSerializer New(final SerializerFoundation<?> foundation)
	{
		return new TypedStreamSerializer.Default(foundation);
	}
	
	
	
	public static class Default implements TypedStreamSerializer
	{
		private final SerializerFoundation<?> foundation;
		
		Default(final SerializerFoundation<?> foundation)
		{
			super();
			this.foundation = foundation;
		}
		
		@Override
		public void serialize(final Object object, final OutputStream out)
		{
			TypedSerializer.New(
				this.foundation,
				binary -> {
					this.write(binary, out);
					return null;
				},
				medium -> null
			)
			.serialize(object);
		}
		
		@Override
		public <T> T deserialize(final InputStream in)
		{
			return TypedSerializer.New(
				this.foundation,
				binary -> null,
				this::read
			)
			.deserialize(in);
		}
		
		private void write(final Binary binary, final OutputStream out)
		{
			for(final ByteBuffer buffer : binary.buffers())
			{
				final int currentSourcePosition = buffer.position();
				
				final WritableByteChannel channel = Channels.newChannel(out);
				try
				{
					while(buffer.hasRemaining())
					{
						channel.write(buffer);
					}
					out.flush();
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}
				
				buffer.position(currentSourcePosition);
			}
		}
		
		private Binary read(final InputStream in)
		{
			if(in instanceof FileInputStream)
			{
				try
				{
					final FileChannel channel = ((FileInputStream)in).getChannel();
					if(channel.size() <= Integer.MAX_VALUE)
					{
						final ByteBuffer buffer = XIO.read(channel);
						return ChunksWrapper.New(buffer);
					}
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}
			}
			
			final BulkList<ByteBuffer> buffers  = BulkList.New();
			ByteBuffer buffer = null;
			
			// TODO: read type info
			
			try
			{
				final DataInput din = new DataInputStream(in);
				
				while(true)
				{
					long length = din.readLong();
					if(XMemory.nativeByteOrder() == ByteOrder.LITTLE_ENDIAN)
					{
						length = Long.reverseBytes(length);
					}
					if(length > 0)
					{
						if(buffer == null || buffer.remaining() < length)
						{
							if(buffer != null && buffer.position() > 0)
							{
								buffer.flip();
								buffers.add(buffer);
							}
							buffer = XMemory.allocateDirectNative(Math.max(length, 1024 * 1024));
						}
						buffer.putLong(length);
						buffer.put(
							in.readNBytes(X.checkArrayRange(length - Binary.lengthLength()))
						);
					}
					else if(length < 0)
					{
						in.skip(-length);
					}
				}
			}
			catch(final EOFException eof)
			{
				// end of stream, just continue
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			if(buffer != null && buffer.position() > 0)
			{
				buffer.flip();
				buffers.add(buffer);
			}
			
			return ChunksWrapper.New(buffers.toArray(ByteBuffer.class));
		}
		
	}
	
}
