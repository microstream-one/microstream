
package one.microstream.memory;

public interface MemoryStatisticsProvider
{
	public MemoryStatistics heapMemoryUsage();
	
	public MemoryStatistics nonHeapMemoryUsage();
	
	
	public static MemoryStatisticsProvider set(final MemoryStatisticsProvider memoryStatisticsProvider)
	{
		return Static.set(memoryStatisticsProvider);
	}
	
	public static MemoryStatisticsProvider get()
	{
		return Static.get();
	}
	
	
	public final class Static
	{
		public final static long DEFAULT_UPDATE_INTERVAL = 1000;
		
		static MemoryStatisticsProvider globalMemoryStatisticsProvider = New(DEFAULT_UPDATE_INTERVAL);
		
		static synchronized MemoryStatisticsProvider set(final MemoryStatisticsProvider memoryStatisticsProvider)
		{
			final MemoryStatisticsProvider old = globalMemoryStatisticsProvider;
			globalMemoryStatisticsProvider = memoryStatisticsProvider;
			return old;
		}
		
		static synchronized MemoryStatisticsProvider get()
		{
			return globalMemoryStatisticsProvider;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		/**
		* Dummy constructor to prevent instantiation of this static-only utility class.
		* 
		* @throws UnsupportedOperationException
		*/
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
	
	
	public static MemoryStatisticsProvider New(final long updateInterval)
	{
		return new Default(updateInterval);
	}
	
	
	public static class Default implements MemoryStatisticsProvider
	{
		private final long             updateInterval                            ;

		private final Object           heapMemoryLock              = new Object();
		private       MemoryStatistics heapMemoryUsage                           ;
		private       long             heapMemoryUsageTimestamp                  ;

		private final Object           nonHeapMemoryLock           = new Object();
		private       MemoryStatistics nonHeapMemoryUsage                        ;
		private       long             nonHeapMemoryUsageTimestamp               ;
		
		Default(final long updateInterval)
		{
			super();
			
			this.updateInterval = updateInterval;
		}
		
		@Override
		public MemoryStatistics heapMemoryUsage()
		{
			final long now = System.currentTimeMillis();
			
			synchronized(this.heapMemoryLock)
			{
				if(this.heapMemoryUsage == null
				|| now - this.updateInterval >= this.heapMemoryUsageTimestamp
				)
				{
					this.heapMemoryUsageTimestamp = now                               ;
					this.heapMemoryUsage          = MemoryStatistics.HeapMemoryUsage();
				}
				
				return this.heapMemoryUsage;
			}
		}
		
		@Override
		public MemoryStatistics nonHeapMemoryUsage()
		{
			final long now = System.currentTimeMillis();
			
			synchronized(this.nonHeapMemoryLock)
			{
				if(this.nonHeapMemoryUsage == null
				|| now - this.updateInterval >= this.nonHeapMemoryUsageTimestamp
				)
				{
					this.nonHeapMemoryUsageTimestamp = now                               ;
					this.nonHeapMemoryUsage          = MemoryStatistics.HeapMemoryUsage();
				}
				
				return this.nonHeapMemoryUsage;
			}
		}
		
	}
	
}
