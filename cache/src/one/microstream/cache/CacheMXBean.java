
package one.microstream.cache;

import javax.cache.Cache;
import javax.cache.configuration.CompleteConfiguration;


public interface CacheMXBean extends javax.cache.management.CacheMXBean
{
	public static CacheMXBean New(final Cache<?, ?> cache)
	{
		return new Default(cache);
	}
	
	public static class Default implements CacheMXBean
	{
		private final Cache<?, ?> cache;
		
		Default(final Cache<?, ?> cache)
		{
			super();
			
			this.cache = cache;
		}
		
		@SuppressWarnings("unchecked")
		private CompleteConfiguration<?, ?> configuration()
		{
			return this.cache.getConfiguration(CompleteConfiguration.class);
		}
		
		@Override
		public String getKeyType()
		{
			return this.configuration().getKeyType().getName();
		}
		
		@Override
		public String getValueType()
		{
			return this.configuration().getValueType().getName();
		}
		
		@Override
		public boolean isReadThrough()
		{
			return this.configuration().isReadThrough();
		}
		
		@Override
		public boolean isWriteThrough()
		{
			return this.configuration().isWriteThrough();
		}
		
		@Override
		public boolean isStoreByValue()
		{
			return this.configuration().isStoreByValue();
		}
		
		@Override
		public boolean isStatisticsEnabled()
		{
			return this.configuration().isStatisticsEnabled();
		}
		
		@Override
		public boolean isManagementEnabled()
		{
			return this.configuration().isManagementEnabled();
		}
		
	}
	
}
