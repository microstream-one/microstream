package one.microstream.util;

import java.lang.management.ManagementFactory;

/**
 * Provides an arbitrary identity string that is unique for an individual process accross any number of systems.
 * 
 * 
 */
public interface ProcessIdentityProvider
{
	public String provideProcessIdentity();
	
	
	
	public static String queryProcessIdentity()
	{
		// quickly googled solution that is assumed to be "good enough" until proven otherwise.
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	
	
	public static ProcessIdentityProvider New()
	{
		return new ProcessIdentityProvider.Default();
	}
	
	public final class Default implements ProcessIdentityProvider
	{
		Default()
		{
			super();
		}

		@Override
		public String provideProcessIdentity()
		{
			return ProcessIdentityProvider.queryProcessIdentity();
		}
		
	}
}
