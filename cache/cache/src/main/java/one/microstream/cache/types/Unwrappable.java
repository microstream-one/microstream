
package one.microstream.cache.types;

public interface Unwrappable
{
	public <T> T unwrap(final Class<T> clazz);
	
	final class Static
	{
		public static <T> T unwrap(final Object subject, final Class<T> clazz)
		{
			if(clazz.isAssignableFrom(subject.getClass()))
			{
				return clazz.cast(subject);
			}
			throw new IllegalArgumentException("Unwrapping to " + clazz + " is not supported by this implementation");
		}
		
		private Static()
		{
			throw new Error();
		}
		
	}
	
}
