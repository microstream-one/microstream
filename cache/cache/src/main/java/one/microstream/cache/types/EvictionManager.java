
package one.microstream.cache.types;

import static one.microstream.X.notNull;

import java.lang.ref.WeakReference;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryCreatedListener;

import one.microstream.reference._longReference;
import one.microstream.typing.KeyValue;


public interface EvictionManager<K, V>
{
	public void install(Cache<K, V> cache, CacheTable cacheTable);
	
	public void uninstall(Cache<K, V> cache, CacheTable cacheTable);
	
	
	public static <K, V> EvictionManager<K, V> OnEntryCreation(final EvictionPolicy evictionPolicy)
	{
		return new OnEntryCreation<>(evictionPolicy);
	}
	
	public static <K, V> EvictionManager<K, V> Interval(
		final EvictionPolicy evictionPolicy,
		final long milliTimeInterval
	)
	{
		return new Interval<>(evictionPolicy, () -> milliTimeInterval);
	}
	
	public static <K, V> EvictionManager<K, V> Interval(
		final EvictionPolicy evictionPolicy,
		final _longReference milliTimeIntervalProvider
	)
	{
		return new Interval<>(evictionPolicy, milliTimeIntervalProvider);
	}
	
	
	public static abstract class Abstract<K, V> implements EvictionManager<K, V>
	{
		final EvictionPolicy evictionPolicy;

		Abstract(final EvictionPolicy evictionPolicy)
		{
			super();
			
			this.evictionPolicy = notNull(evictionPolicy);
		}
		
		void evict(
			final Cache<K, V> cache,
			final CacheTable  cacheTable
		)
		{
			final Iterable<KeyValue<Object, CachedValue>> entriesToEvict;
			if((entriesToEvict = this.evictionPolicy.pickEntriesToEvict(cacheTable)) != null)
			{
				cache.evict(entriesToEvict);
			}
		}
	}
		
	
	public static class OnEntryCreation<K, V> extends Abstract<K, V>
	{
		private CacheEntryListenerConfiguration<K, V> listenerConfiguration;
		
		OnEntryCreation(final EvictionPolicy evictionPolicy)
		{
			super(evictionPolicy);
		}
		
		@Override
		public void install(final Cache<K, V> cache, final CacheTable cacheTable)
		{
			if(this.listenerConfiguration == null)
			{
				final CacheEntryCreatedListener<K, V> entryCreatedListener = events ->
					events.forEach(event -> this.evict(cache, cacheTable))
				;
				
				this.listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
					() -> entryCreatedListener,
					null,  // no filter required
					false, // no old value required
					true   // synchronous
				);
				
				cache.registerCacheEntryListener(this.listenerConfiguration);
			}
		}
		
		@Override
		public void uninstall(final Cache<K, V> cache, final CacheTable cacheTable)
		{
			if(this.listenerConfiguration != null)
			{
				cache.deregisterCacheEntryListener(this.listenerConfiguration);
				
				this.listenerConfiguration = null;
			}
		}
		
	}
	
	
	public static class Interval<K, V> extends Abstract<K, V>
	{
		private final    _longReference milliTimeIntervalProvider;
		private Cache<K, V>             cache;
		private CacheTable              cacheTable;
		private volatile boolean        running;

		Interval(
			final EvictionPolicy evictionPolicy,
			final _longReference milliTimeIntervalProvider
		)
		{
			super(evictionPolicy);
			
			this.milliTimeIntervalProvider = milliTimeIntervalProvider;
		}

		@Override
		public void install(final Cache<K, V> cache, final CacheTable cacheTable)
		{
			this.cache      = cache;
			this.cacheTable = cacheTable;
			
			if(!this.running)
			{
				this.running = true;
				new IntervalThread(new WeakReference<>(this), this.milliTimeIntervalProvider).start();
			}
		}

		@Override
		public void uninstall(final Cache<K, V> cache, final CacheTable cacheTable)
		{
			this.running    = false;
			this.cache      = null;
			this.cacheTable = null;
		}
		
		void evict()
		{
			this.evict(this.cache, this.cacheTable);
		}
		
		
		static final class IntervalThread extends Thread
		{
			// lazy reference for automatic thread termination
			private final WeakReference<EvictionManager.Interval<?, ?>> parent;
			private final _longReference                                milliTimeIntervalProvider;
			
			IntervalThread(
				final WeakReference<EvictionManager.Interval<?, ?>> parent,
				final _longReference                                milliTimeIntervalProvider
			)
			{
				super(EvictionManager.class.getSimpleName()+"@"+System.identityHashCode(parent));
				
				this.parent                    = parent;
				this.milliTimeIntervalProvider = milliTimeIntervalProvider;
			}
			
			@Override
			public void run()
			{
				EvictionManager.Interval<?, ?> parent;
				while((parent = this.parent.get()) != null)
				{
					// sleep for a dynamically specified milli time until the next check
					try
					{
						// check for running state. Must be the first action in case of swallowed exception
						if(!parent.running)
						{
							break;
						}

						// perform eviction
						parent.evict();

						// very nasty: must clear the reference from the stack in order for the weak reference to work
						parent = null;

						// extra nasty: must sleep with nulled reference for WR to work, not before.
						try
						{
							Thread.sleep(this.milliTimeIntervalProvider.get());
						}
						catch(final InterruptedException e)
						{
							// sleep interrupted, proceed with check immediately
						}
					}
					catch(final Exception e)
					{
						/* thread may not die on any exception, just continue looping as long as parent exists
						 * and running is true
						 */
					}
				}
				// either parent has been garbage collected or stopped, so terminate.
			}
			
		}
		
	}
	
}
