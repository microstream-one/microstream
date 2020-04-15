
package one.microstream.entity;

import static one.microstream.X.mayNull;

import java.time.Instant;

import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;

/**
 * 
 * @author FH
 */
public interface EntityVersionContext<K> extends EntityLayerProviderProvider
{
	public K currentVersion();
	
	public default K versionForUpdate()
	{
		return this.currentVersion();
	}
	
	public HashEqualator<? super K> equalator();
	
	public EntityVersionCleaner<K> cleaner();
	
	@SuppressWarnings("rawtypes")
	public default <E extends Entity> XGettingTable<K, E> versions(final E entity)
	{
		Entity layer = entity;
		while(layer instanceof EntityLayer)
		{
			if(layer instanceof EntityLayerVersioning)
			{
				return ((EntityLayerVersioning)layer).versions();
			}
			
			layer = ((EntityLayer)layer).inner();
		}
		return null;
	}
	
	@Override
	public default EntityLayerProvider provideEntityLayerProvider()
	{
		return e -> new EntityLayerVersioning<>(e, this);
	}
	
	public static abstract class Abstract<K> implements EntityVersionContext<K>
	{
		private final EntityVersionCleaner<K> cleaner;
		protected K                           key;
		
		protected Abstract(final EntityVersionCleaner<K> cleaner)
		{
			super();			
			this.cleaner = mayNull(cleaner);
		}
		
		@Override
		public K currentVersion()
		{
			return this.key;
		}
		
		@Override
		public HashEqualator<? super K> equalator()
		{
			return XHashing.hashEqualityValue();
		}
		
		@Override
		public EntityVersionCleaner<K> cleaner()
		{
			return this.cleaner;
		}
	}
	
	public static <K> EntityVersionContext.Mutable<K> Mutable()
	{
		return new Mutable.Default<>(null);
	}
	
	public static <K> EntityVersionContext.Mutable<K> Mutable(
		final EntityVersionCleaner<K> cleaner
	)
	{
		return new Mutable.Default<>(cleaner);
	}
	
	public static interface Mutable<K> extends EntityVersionContext<K>
	{
		public EntityVersionContext<K> currentVersion(K key);
		
		public static class Default<K>
			extends EntityVersionContext.Abstract<K>
			implements Mutable<K>
		{
			protected Default(final EntityVersionCleaner<K> cleaner)
			{
				super(cleaner);
			}
			
			@Override
			public EntityVersionContext<K> currentVersion(final K key)
			{
				this.key = key;
				
				return this;
			}
		}
	}
	
	public static EntityVersionContext<Integer> AutoIncrementingInt()
	{
		return new AutoIncrementing.IntIncrementor(null);
	}
	
	public static EntityVersionContext<Integer> AutoIncrementingInt(
		final EntityVersionCleaner<Integer> cleaner
	)
	{
		return new AutoIncrementing.IntIncrementor(cleaner);
	}
	
	public static EntityVersionContext<Long> AutoIncrementingLong()
	{
		return new AutoIncrementing.LongIncrementor(null);
	}
	
	public static EntityVersionContext<Long> AutoIncrementingLong(
		final EntityVersionCleaner<Long> cleaner
	)
	{
		return new AutoIncrementing.LongIncrementor(cleaner);
	}
	
	public static EntityVersionContext<Long> AutoIncrementingSystemTimeMillis()
	{
		return new AutoIncrementing.SystemTimeMillis(null);
	}
	
	public static EntityVersionContext<Long> AutoIncrementingSystemTimeMillis(
		final EntityVersionCleaner<Long> cleaner
	)
	{
		return new AutoIncrementing.SystemTimeMillis(cleaner);
	}
	
	public static EntityVersionContext<Long> AutoIncrementingSystemNanoTime()
	{
		return new AutoIncrementing.SystemNanoTime(null);
	}
	
	public static EntityVersionContext<Long> AutoIncrementingSystemNanoTime(
		final EntityVersionCleaner<Long> cleaner
	)
	{
		return new AutoIncrementing.SystemNanoTime(cleaner);
	}
	
	public static EntityVersionContext<Instant> AutoIncrementingInstant()
	{
		return new AutoIncrementing.InstantIncrementor(null);
	}
	
	public static EntityVersionContext<Instant> AutoIncrementingInstant(
		final EntityVersionCleaner<Instant> cleaner
	)
	{
		return new AutoIncrementing.InstantIncrementor(cleaner);
	}
	
	public static interface AutoIncrementing<K> extends EntityVersionContext<K>
	{
		@Override
		public K versionForUpdate();
		
		public static class IntIncrementor
			extends EntityVersionContext.Abstract<Integer>
			implements AutoIncrementing<Integer>
		{
			protected IntIncrementor(final EntityVersionCleaner<Integer> cleaner)
			{
				super(cleaner);
			}
			
			@Override
			public Integer versionForUpdate()
			{
				return this.key =
					this.key == null
						? 0
						: this.key + 1
				;
			}
		}
		
		public static class LongIncrementor
			extends EntityVersionContext.Abstract<Long>
			implements AutoIncrementing<Long>
		{
			protected LongIncrementor(final EntityVersionCleaner<Long> cleaner)
			{
				super(cleaner);
			}
			
			@Override
			public Long versionForUpdate()
			{
				return this.key =
					this.key == null
						? 0L
						: this.key + 1L
				;
			}
		}
		
		public static class SystemTimeMillis
			extends EntityVersionContext.Abstract<Long>
			implements AutoIncrementing<Long>
		{
			protected SystemTimeMillis(final EntityVersionCleaner<Long> cleaner)
			{
				super(cleaner);
			}
			
			@Override
			public Long versionForUpdate()
			{
				return this.key = System.currentTimeMillis();
			}
		}
		
		public static class SystemNanoTime
			extends EntityVersionContext.Abstract<Long>
			implements AutoIncrementing<Long>
		{
			protected SystemNanoTime(final EntityVersionCleaner<Long> cleaner)
			{
				super(cleaner);
			}
			
			@Override
			public Long versionForUpdate()
			{
				return this.key = System.nanoTime();
			}
		}
		
		public static class InstantIncrementor
			extends EntityVersionContext.Abstract<Instant>
			implements AutoIncrementing<Instant>
		{
			protected InstantIncrementor(final EntityVersionCleaner<Instant> cleaner)
			{
				super(cleaner);
			}
			
			@Override
			public Instant versionForUpdate()
			{
				return this.key = Instant.now();
			}
		}
		
	}
	
}
