package one.microstream.afs.redis;

import java.nio.ByteBuffer;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

public interface StringByteBufferCodec extends RedisCodec<String, ByteBuffer>
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

		@Override
		public ByteBuffer encodeKey(
			final String key
		)
		{
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
			return value;
		}

		@Override
		public ByteBuffer decodeValue(
			final ByteBuffer bytes
		)
		{
			return bytes;
		}

	}

}
