
package one.microstream.cache.types;

import javax.cache.configuration.CompleteConfiguration;


public interface CacheConfigurationMXBean extends javax.cache.management.CacheMXBean
{
	public static class Default implements CacheConfigurationMXBean
	{
		private final CompleteConfiguration<?, ?> configuration;
		
		Default(final CompleteConfiguration<?, ?> configuration)
		{
			super();
			
			this.configuration = configuration;
		}
		
		@Override
		public String getKeyType()
		{
			return this.configuration.getKeyType().getName();
		}
		
		@Override
		public String getValueType()
		{
			return this.configuration.getValueType().getName();
		}
		
		@Override
		public boolean isReadThrough()
		{
			return this.configuration.isReadThrough();
		}
		
		@Override
		public boolean isWriteThrough()
		{
			return this.configuration.isWriteThrough();
		}
		
		@Override
		public boolean isStoreByValue()
		{
			return this.configuration.isStoreByValue();
		}
		
		@Override
		public boolean isStatisticsEnabled()
		{
			return this.configuration.isStatisticsEnabled();
		}
		
		@Override
		public boolean isManagementEnabled()
		{
			return this.configuration.isManagementEnabled();
		}
		
	}
	
}
