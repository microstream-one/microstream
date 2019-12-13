
package one.microstream.cache;

import java.lang.ref.WeakReference;


public interface ObjectConverter<T>
{
	public Object toInternal(T value);
	
	public T fromInternal(Object internal);
	
	public static class ByReference<T> implements ObjectConverter<T>
	{
		ByReference()
		{
			super();
		}
		
		@Override
		public Object toInternal(final T value)
		{
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T fromInternal(final Object internal)
		{
			return (T)internal;
		}
	}
	
	public static class ByValue<T> implements ObjectConverter<T>
	{
		private final WeakReference<ClassLoader> classLoaderRef;
		
		ByValue(final WeakReference<ClassLoader> classLoaderRef)
		{
			super();
			
			this.classLoaderRef = classLoaderRef;
		}
		
		@Override
		public Object toInternal(final T value)
		{
			
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T fromInternal(final Object internal)
		{
			
		}
	}
}
