package one.microstream.afs.redis.types;

import java.nio.ByteBuffer;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.ToByteBufEncoder;
import io.netty.buffer.ByteBuf;

public interface StringByteBufferCodec extends RedisCodec<String, ByteBuffer>, ToByteBufEncoder<String, ByteBuffer>
{

	public static StringByteBufferCodec New()
	{
		return new StringByteBufferCodec.Default();
	}


	public static class Default implements StringByteBufferCodec
	{
		Default()
		{
			super();
		}
		
		private ByteBuffer copy(
			final ByteBuffer buffer
		)
		{
			final int        pos  = buffer.position();
			final ByteBuffer copy = ByteBuffer.allocateDirect(buffer.limit());
	        
	        buffer.rewind();

	        copy.order(buffer.order());
	        copy.put(buffer);
	        copy.position(pos);

	        buffer.position(pos);

	        return copy;
	    }

		@Override
		public ByteBuffer encodeKey(
			final String key
		)
		{
			if(key == null)
			{
				return ByteBuffer.allocate(0);
			}
			
			return StringCodec.UTF8.encodeKey(key);
		}

		@Override
		public String decodeKey(
			final ByteBuffer bytes
		)
		{
			return StringCodec.UTF8.decodeKey(bytes);
		}

		@Override
		public ByteBuffer encodeValue(
			final ByteBuffer value
		)
		{
			if(value == null)
			{
				return ByteBuffer.allocate(0);
			}
			
			return this.copy(value);
		}

		@Override
		public ByteBuffer decodeValue(
			final ByteBuffer bytes
		)
		{
			if(bytes == null)
			{
				return ByteBuffer.allocate(0);
			}
			
			return this.copy(bytes);
		}

		@Override
		public void encodeKey(
			final String  key   ,
			final ByteBuf target
		)
		{
			if(key != null)
			{
				StringCodec.UTF8.encodeKey(key, target);
			}
		}

		@Override
		public void encodeValue(
			final ByteBuffer value ,
			final ByteBuf    target
		)
		{
			if(value != null)
			{
				target.writeBytes(value);
				value.flip();
			}
		}

		@Override
		public int estimateSize(
			final Object keyOrValue
		)
		{
			if(keyOrValue instanceof String)
			{
				return StringCodec.UTF8.estimateSize(keyOrValue);
			}
			if(keyOrValue instanceof ByteBuffer)
			{
				return ((ByteBuffer)keyOrValue).limit();
			}
			
			return 0;
		}

	}

}
