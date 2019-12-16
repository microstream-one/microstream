
package one.microstream.cache;

public interface ObjectConverter
{
	public <T> Object toInternal(T value);
	
	public <T> T fromInternal(Object internal);
	
	public static ObjectConverter ByReference()
	{
		return new ByReference();
	}
	
	public static ObjectConverter ByValue(final Serializer serializer)
	{
		return new ByValue(serializer);
	}
	
	public static class ByReference implements ObjectConverter
	{
		ByReference()
		{
			super();
		}
		
		@Override
		public <T> Object toInternal(final T value)
		{
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T fromInternal(final Object internal)
		{
			return (T)internal;
		}
	}
	
	public static class ByValue implements ObjectConverter
	{
		private final Serializer serializer;
		
		ByValue(final Serializer serializer)
		{
			super();
			
			this.serializer = serializer;
		}
		
		@Override
		public <T> Object toInternal(final T value)
		{
			final byte[] data = this.serializer.write(value);
			return SerializedObject.New(value.hashCode(), data);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T fromInternal(final Object internal)
		{
			final byte[] data = ((SerializedObject)internal).serializedData();
			return (T)this.serializer.read(data);
		}
	}
}
