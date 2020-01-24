
package one.microstream.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/*
 * MemoryMXBean is used to determine memory usage, because it is considerably faster than Runtime#*memory()
 */
/**
 * 
 * @author FH
 */
public interface MemoryStatistics
{
	public long max();
	
	public long used();
	
	public long available();
	
	public double quota();
	
	
	
	public static MemoryStatistics HeapMemoryUsage()
	{
		return New(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
	}
	
	public static MemoryStatistics NonHeapMemoryUsage()
	{
		return New(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
	}
	
	public static MemoryStatistics New(final MemoryUsage usage)
	{
		final long max       = usage.getMax() ;
		final long used      = usage.getUsed();
		final long available = max - used     ;
		
		return new Default(max, used, available, (double)available / (double)max);
	}
		
	public static class Default implements MemoryStatistics
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long   max      ;
		private final long   used     ;
		private final long   available;
		private final double quota    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final long max      ,
			final long used     ,
			final long available,
			final double quota
		)
		{
			super();
			
			this.max       = max      ;
			this.used      = used     ;
			this.available = available;
			this.quota     = quota    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public long max()
		{
			return this.max;
		}
		
		@Override
		public long used()
		{
			return this.used;
		}
		
		@Override
		public long available()
		{
			return this.available;
		}
		
		@Override
		public double quota()
		{
			return this.quota;
		}
		
	}
	
}
