
package one.microstream.cache;

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
	public void addEvent(
		final Class<? extends CacheEntryListener> listenerClass,
		final CacheEvent<K, V> event);
	
	public void dispatch(Iterable<CacheEntryListenerRegistration<K, V>> registrations);
	
	public static <K, V> CacheEventDispatcher<K, V> New()
	{
		return new Default<>();
	}
	
	public static class Default<K, V> implements CacheEventDispatcher<K, V>
	{
		@SuppressWarnings("rawtypes")
		private final XTable<Class<? extends CacheEntryListener>, XList<CacheEvent<K, V>>> eventMap;
		
		Default()
		{
			super();
			
			this.eventMap = EqHashTable.New();
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public synchronized void addEvent(
			final Class<? extends CacheEntryListener> listenerClass,
			final CacheEvent<K, V> event)
		{
			notNull(listenerClass);
			notNull(event);
			
			this.eventMap
				.ensure(listenerClass, c -> BulkList.New())
				.add(event);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public synchronized void dispatch(final Iterable<CacheEntryListenerRegistration<K, V>> registrations)
		{
			this.dispatch(registrations, CacheEntryExpiredListener.class, CacheEntryExpiredListener::onExpired);
			this.dispatch(registrations, CacheEntryCreatedListener.class, CacheEntryCreatedListener::onCreated);
			this.dispatch(registrations, CacheEntryUpdatedListener.class, CacheEntryUpdatedListener::onUpdated);
			this.dispatch(registrations, CacheEntryRemovedListener.class, CacheEntryRemovedListener::onRemoved);
		}
		
		@SuppressWarnings("unchecked")
		private <L extends CacheEntryListener<? super K, ? super V>> void dispatch(
			final Iterable<CacheEntryListenerRegistration<K, V>> registrations,
			final Class<L> type,
			final BiConsumer<L, Iterable<CacheEntryEvent<? extends K, ? extends V>>> logic)
		{
			final XList<CacheEvent<K, V>> events = this.eventMap.get(type);
			if(events != null)
			{
				for(final CacheEntryListenerRegistration<K, V> registration : registrations)
				{
					final CacheEntryListener<? super K, ? super V> listener =
						registration.getCacheEntryListener();
					if(type.isInstance(listener))
					{
						final Iterable<CacheEntryEvent<? extends K, ? extends V>> selectedEvents =
							this.selectEvents(registration, events);
						logic.accept(type.cast(listener), selectedEvents);
					}
				}
			}
		}
		
		@SuppressWarnings("rawtypes")
		private Iterable selectEvents(
			final CacheEntryListenerRegistration<K, V> registration,
			final XList<CacheEvent<K, V>> events)
		{
			CqlSelection<CacheEvent<K, V>>                    selection = CQL.from(events);
			final CacheEntryEventFilter<? super K, ? super V> filter    = registration.getCacheEntryFilter();
			if(filter != null)
			{
				selection = selection.select(e -> filter.evaluate(e));
			}
			return selection
				.project(e -> this.cloneEvent(registration, e))
				.into(BulkList.New())
				.execute();
		}
		
		private CacheEvent<K, V> cloneEvent(
			final CacheEntryListenerRegistration<K, V> registration,
			final CacheEvent<K, V> event)
		{
			if(registration.isOldValueRequired())
			{
				return new CacheEvent<>(
					event.getCache(),
					event.getEventType(),
					event.getKey(),
					event.getValue(),
					event.getOldValue());
			}
			else if(event.getEventType() == EventType.REMOVED || event.getEventType() == EventType.EXPIRED)
			{
				return new CacheEvent<>(
					event.getCache(),
					event.getEventType(),
					event.getKey(),
					null);
			}
			
			return new CacheEvent<>(
				event.getCache(),
				event.getEventType(),
				event.getKey(),
				event.getValue());
		}
		
	}
	
}
