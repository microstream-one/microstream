
package one.microstream.cache.types;

public interface ObjectConverter
{
	public <T> Object internalize(T value);
	
	public <T> T externalize(Object internal);
	
	
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
		public <T> Object internalize(final T value)
		{
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T externalize(final Object internal)
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
		public <T> Object internalize(final T value)
		{
			return SerializedObject.New(
				value.hashCode(),
				this.serializer.write(value)
			);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T externalize(final Object internal)
		{
			return (T)this.serializer.read(
				((SerializedObject)internal).serializedData()
			);
		}
		
	}
	
}
