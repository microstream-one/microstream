package one.microstream.cache.hibernate.types;

import static one.microstream.X.notNull;

import javax.cache.CacheException;

import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import one.microstream.cache.types.Cache;


public interface StorageAccess extends DomainDataStorageAccess
{
	public static StorageAccess New(
		final Cache<Object, Object> cache
	)
	{
		return new Default(notNull(cache));
	}
	
	
	public static class Default implements StorageAccess
	{
		private final Cache<Object, Object> cache;
		
		Default(
			final Cache<Object, Object> cache
		)
		{
			super();
			this.cache = cache;
		}
		
		@Override
		public Object getFromCache(
			final Object key, 
			final SharedSessionContractImplementor session
		)
		{
			try
			{
				return this.cache.get(key);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void putIntoCache(
			final Object key, 
			final Object value, 
			final SharedSessionContractImplementor session
		)
		{
			try
			{
				this.cache.put(key, value);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public boolean contains(
			final Object key
		)
		{
			try
			{
				return this.cache.containsKey(key);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void evictData()
		{
			try
			{
				this.cache.removeAll();
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void evictData(
			final Object key
		)
		{
			try
			{
				this.cache.remove(key);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void release()
		{
			try
			{
				this.cache.close();
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
	}
	
}
