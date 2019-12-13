
package one.microstream.cache;

import javax.cache.configuration.CompleteConfiguration;


public interface CacheConfiguration<K, V> extends CompleteConfiguration<K, V>
{
	public static class Default
	{
		// hashCode/equals must be implemented according to spec
		
		@Override
		public boolean equals(final Object obj)
		{
			// TODO Auto-generated method stub
			return super.equals(obj);
		}
		
		@Override
		public int hashCode()
		{
			// TODO Auto-generated method stub
			return super.hashCode();
		}
	}
}
