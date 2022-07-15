
package one.microstream.memory;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		/* (24.01.2020 TM)NOTE:
		 * No public constant field ... and actually, no constant field at all, because why occupy memory twice?
		 */
		public static final long defaultUpdateInterval()
		{
			return 1000;
		}
		
		static MemoryStatisticsProvider globalMemoryStatisticsProvider = MemoryStatisticsProvider.New(defaultUpdateInterval());
		
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
		* @throws UnsupportedOperationException when called
		*/
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
	
	
	
	public static MemoryStatisticsProvider New(final long updateInterval)
	{
		return new MemoryStatisticsProvider.Default(updateInterval);
	}
	
	public static class Default implements MemoryStatisticsProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long             updateInterval                            ;

		private final Object           heapMemoryLock              = new Object();
		private       MemoryStatistics heapMemoryUsage                           ;
		private       long             heapMemoryUsageTimestamp                  ;

		private final Object           nonHeapMemoryLock           = new Object();
		private       MemoryStatistics nonHeapMemoryUsage                        ;
		private       long             nonHeapMemoryUsageTimestamp               ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final long updateInterval)
		{
			super();
			
			this.updateInterval = updateInterval;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public MemoryStatistics heapMemoryUsage()
		{
			final long now = System.currentTimeMillis();
			
			synchronized(this.heapMemoryLock)
			{
				if(this.heapMemoryUsage == null || now - this.updateInterval >= this.heapMemoryUsageTimestamp)
				{
					this.heapMemoryUsageTimestamp = now                                                  ;
					this.heapMemoryUsage          = XMemory.memoryAccessor().createHeapMemoryStatistics();
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
				if(this.nonHeapMemoryUsage == null || now - this.updateInterval >= this.nonHeapMemoryUsageTimestamp)
				{
					this.nonHeapMemoryUsageTimestamp = now                                                     ;
					this.nonHeapMemoryUsage          = XMemory.memoryAccessor().createNonHeapMemoryStatistics();
				}
				
				return this.nonHeapMemoryUsage;
			}
		}
		
	}
	
}
