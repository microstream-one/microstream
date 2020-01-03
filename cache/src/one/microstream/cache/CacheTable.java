
package one.microstream.cache;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

import one.microstream.collections.EqHashTable;
import one.microstream.typing.KeyValue;


public interface CacheTable
{
	public CachedValue get(Object key);
	
	public boolean put(Object key, CachedValue value);
	
	public CachedValue remove(Object key);
	
	public Iterable<Object> keys();
	
	public Iterator<KeyValue<Object, CachedValue>> iterator();
	
	public KeyValue<Object, CachedValue> search(Predicate<? super KeyValue<Object, CachedValue>> predicate);
	
	public long size();
	
	public void clear();
	
	public KeyValue<Object, CachedValue> min(Comparator<? super KeyValue<Object, CachedValue>> comparator);
	
	public KeyValue<Object, CachedValue> rangeMin(long offset, long length, Comparator<? super KeyValue<Object, CachedValue>> comparator);
	
	
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
			return this.table.iterator();
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
			return this.table.rangeMin(offset, length, comparator);
		}
		
	}
	
}
