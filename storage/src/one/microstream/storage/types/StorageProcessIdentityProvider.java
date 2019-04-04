package one.microstream.storage.types;

import java.lang.management.ManagementFactory;

/**
 * Provides an arbitrary identity string that is unique for an individual process accross any number of systems.
 * 
 * @author TM
 */
public interface StorageProcessIdentityProvider
{
	public String provideProcessIdentity();
	
	
	
	public static String queryProcessIdentity()
	{
		// quickly googled solution that is assumed to be "good enough" until proven otherwise.
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	
	
	public static StorageProcessIdentityProvider New()
	{
		return new StorageProcessIdentityProvider.Default();
	}
	
	public final class Default implements StorageProcessIdentityProvider
	{
		Default()
		{
			super();
		}

		@Override
		public String provideProcessIdentity()
		{
			return StorageProcessIdentityProvider.queryProcessIdentity();
		}
		
	}
}
