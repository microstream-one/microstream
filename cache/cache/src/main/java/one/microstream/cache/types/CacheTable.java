
package one.microstream.cache.types;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.functional.Aggregator;
import one.microstream.typing.KeyValue;


public interface CacheTable
{
	public CachedValue get(
		Object key
	);
	
	public boolean put(
		Object key, 
		CachedValue value
	);
	
	public CachedValue remove(
		Object key
	);
	
	public Iterable<Object> keys();
	
	public Iterator<KeyValue<Object, CachedValue>> iterator();
	
	public void iterate(
		Consumer<KeyValue<Object, CachedValue>> procedure
	);
	
	public KeyValue<Object, CachedValue> search(
		Predicate<? super KeyValue<Object, CachedValue>> predicate
	);
	
	public long size();
	
	public void clear();
	
	public KeyValue<Object, CachedValue> min(
		Comparator<? super KeyValue<Object, CachedValue>> comparator
	);
	
	public KeyValue<Object, CachedValue> rangeMin(
		long offset, 
		long length, 
		Comparator<? super KeyValue<Object, CachedValue>> comparator
	);
			
	
	public static CacheTable New()
	{
		return new Default();
	}
	
	public static class Default implements CacheTable
	{
		private final EqHashTable<Object, CachedValue> table;
		
		Default()
		{
			super();
			
			this.table          = EqHashTable.New();
		}
		
		@Override
		public CachedValue get(final Object key)
		{
			return this.table.get(key);
		}
		
		@Override
		public boolean put(final Object key, final CachedValue value)
		{
			return this.table.put(key, value);
		}
		
		@Override
		public CachedValue remove(final Object key)
		{
			return this.table.removeFor(key);
		}
		
		@Override
		public Iterable<Object> keys()
		{
			return this.table.keys();
		}
		
		@Override
		public Iterator<KeyValue<Object, CachedValue>> iterator()
		{
			Iterator<KeyValue<Object, CachedValue>> it = this.table.iterator();
			/*
			 * Iterator#remove is used by Cache, so we have to implement it
			 * since EqHashTable's iterators don't.
			 */
			return new Iterator<KeyValue<Object, CachedValue>>()
			{
				KeyValue<Object, CachedValue> next;
				
				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}

				@Override
				public KeyValue<Object, CachedValue> next()
				{
					return this.next = it.next();
				}
				
				@Override
				public void remove()
				{
					CacheTable.Default.this.table.remove(this.next);
				}
			};
		}
		
		@Override
		public void iterate(final Consumer<KeyValue<Object, CachedValue>> procedure)
		{
			this.table.iterate(procedure);
		}

		@Override
		public KeyValue<Object, CachedValue> search(final Predicate<? super KeyValue<Object, CachedValue>> predicate)
		{
			return this.table.search(predicate);
		}
		
		@Override
		public long size()
		{
			return this.table.size();
		}
		
		@Override
		public void clear()
		{
			this.table.clear();
		}
		
		@Override
		public KeyValue<Object, CachedValue> min(final Comparator<? super KeyValue<Object, CachedValue>> comparator)
		{
			return this.table.min(comparator);
		}
				
		@Override
		public KeyValue<Object, CachedValue> rangeMin(
			final long offset,
			final long length,
			final Comparator<? super KeyValue<Object, CachedValue>> comparator
		)
		{
			return this.table.iterate(new RangeMin<>(offset, length, comparator)).yield();
		}
		
		
		static class RangeMin<E> implements Aggregator<E, E>
		{
			private final long                  offset, length;
			private final Comparator<? super E> order;
			private long                        iterationOffset, iterationLength;
			private E                           iterationElement;
			
			RangeMin(final long offset, final long length, final Comparator<? super E> order)
			{
				super();
				
				this.offset = offset;
				this.length = length;
				this.order  = order ;
				
				this.reset();
			}
			
			@Override
			public final RangeMin<E> reset()
			{
				this.iterationElement = null;
				this.iterationOffset  = this.offset;
				this.iterationLength  = this.length;
				
				return this;
			}

			@Override
			public final void accept(final E element)
			{
				if(this.iterationOffset > 0)
				{
					this.iterationOffset--;
					return;
				}
				
				if(this.iterationLength <= 0)
				{
					throw X.BREAK();
				}
				
				if(this.iterationLength-- == this.length)
				{
					this.iterationElement = element;
					return;
				}
						
				if(this.order.compare(element, this.iterationElement) < 0)
				{
					this.iterationElement = element;
				}
			}
			
			@Override
			public final E yield()
			{
				return this.iterationElement;
			}
			
		}
		
	}
	
}
