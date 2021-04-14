
package one.microstream.cache.types;

import static one.microstream.X.notNull;

import java.util.function.BiConsumer;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XTable;
import one.microstream.util.cql.CQL;
import one.microstream.util.cql.CqlSelection;


/*
 *  @SuppressWarnings annotations due to insufficient generics in JCache API
 */
public interface CacheEventDispatcher<K, V>
{
	@SuppressWarnings("rawtypes")
	public CacheEventDispatcher<K, V> addEvent(
		final Class<? extends CacheEntryListener> listenerClass,
		final CacheEvent<K, V>                    event
	);
	
	public void dispatch(
		XIterable<CacheEntryListenerRegistration<K, V>> registrations
	);
	
	public static <K, V> CacheEventDispatcher<K, V> New()
	{
		return new Default<>();
	}
	
	public static class Default<K, V> implements CacheEventDispatcher<K, V>
	{
		@SuppressWarnings("rawtypes")
		private XTable<Class<? extends CacheEntryListener>, XList<CacheEvent<K, V>>> eventMap;
		
		Default()
		{
			super();
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public synchronized CacheEventDispatcher<K, V> addEvent(
			final Class<? extends CacheEntryListener> listenerClass,
			final CacheEvent<K, V>                    event
		)
		{
			notNull(listenerClass);
			notNull(event);
			
			// lazy init eventMap
			XTable<Class<? extends CacheEntryListener>, XList<CacheEvent<K, V>>> eventMap;
			if((eventMap = this.eventMap) == null)
			{
				eventMap = this.eventMap = EqHashTable.New();
			}
			eventMap
				.ensure(listenerClass, c -> BulkList.New())
				.add(event);
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public synchronized void dispatch(
			final XIterable<CacheEntryListenerRegistration<K, V>> registrations
		)
		{
			if(this.eventMap == null)
			{
				// no event registered
				return;
			}
			
			this.dispatch(registrations, CacheEntryExpiredListener.class, CacheEntryExpiredListener::onExpired, true);
			this.dispatch(registrations, CacheEntryCreatedListener.class, CacheEntryCreatedListener::onCreated, false);
			this.dispatch(registrations, CacheEntryUpdatedListener.class, CacheEntryUpdatedListener::onUpdated, true);
			this.dispatch(registrations, CacheEntryRemovedListener.class, CacheEntryRemovedListener::onRemoved, true);
		}
		
		@SuppressWarnings("unchecked")
		private <L extends CacheEntryListener<? super K, ? super V>> void dispatch(
			final XIterable<CacheEntryListenerRegistration<K, V>>                    registrations,
			final Class<L>                                                           type,
			final BiConsumer<L, Iterable<CacheEntryEvent<? extends K, ? extends V>>> logic,
			final boolean                                                            oldValueAvailable
		)
		{
			final XList<CacheEvent<K, V>> events;
			if((events = this.eventMap.get(type)) != null)
			{
				registrations.iterate(registration -> 
				{				
					final CacheEntryListener<? super K, ? super V> listener;
					if(type.isInstance(listener = registration.getCacheEntryListener()))
					{
						logic.accept(
							type.cast(listener),
							this.selectEvents(registration, events, oldValueAvailable)
						);
					}
				});
			}
		}
		
		@SuppressWarnings("rawtypes")
		private Iterable selectEvents(
			final CacheEntryListenerRegistration<K, V> registration,
			final XList<CacheEvent<K, V>>              events,
			final boolean                              oldValueAvailable
		)
		{
			CqlSelection<CacheEvent<K, V>>                    selection = CQL.from(events);
			final CacheEntryEventFilter<? super K, ? super V> filter;
			if((filter = registration.getCacheEntryFilter()) != null)
			{
				selection = selection.select(e -> filter.evaluate(e));
			}
			return selection
				.project(e -> this.cloneEvent(registration, e, oldValueAvailable))
				.into(BulkList.New())
				.execute();
		}
		
		private CacheEvent<K, V> cloneEvent(
			final CacheEntryListenerRegistration<K, V> registration,
			final CacheEvent<K, V>                     event,
			final boolean                              oldValueAvailable
		)
		{
			if(oldValueAvailable && registration.isOldValueRequired())
			{
				return new CacheEvent<>(
					event.getCache(),
					event.getEventType(),
					event.getKey(),
					event.getValue(),
					event.getOldValue()
				);
			}
			
			if(event.getEventType() == EventType.REMOVED || event.getEventType() == EventType.EXPIRED)
			{
				return new CacheEvent<>(
					event.getCache(),
					event.getEventType(),
					event.getKey(),
					null
				);
			}
			
			return new CacheEvent<>(
				event.getCache(),
				event.getEventType(),
				event.getKey(),
				event.getValue()
			);
		}
		
	}
	
}
