
package one.microstream.entity.versioning;

import static one.microstream.X.notNull;

import java.time.Instant;
import java.util.Comparator;
import java.util.function.Function;

import one.microstream.entity.EntityLayerProvider;
import one.microstream.entity.EntityLayerProviderProvider;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;


public interface EntityVersionContext<K extends Comparable<? super K>> extends EntityLayerProviderProvider
{
	public K currentVersion();
	
	public default K versionForUpdate()
	{
		return this.currentVersion();
	}
	
	public default long maxPreservedVersions()
	{
		return Long.MAX_VALUE;
	}
	
	public default HashEqualator<? super K> equalator()
	{
		return XHashing.hashEqualityValue();
	}
	
	public default Comparator<? super K> comparator()
	{
		return Comparator.naturalOrder();
	}
	
	@Override
	public default EntityLayerProvider provideEntityLayerProvider()
	{
		return e -> new EntityLayerVersioning<>(e, this);
	}
	
	public static abstract class Abstract<K extends Comparable<? super K>> implements EntityVersionContext<K>
	{
		protected K key;
		
		protected Abstract()
		{
			super();
		}
		
		@Override
		public K currentVersion()
		{
			return this.key;
		}
	}
	
	public static <K extends Comparable<? super K>> EntityVersionContext<K> Mutable()
	{
		return new Mutable.Default<>();
	}
	
	public static interface Mutable<K extends Comparable<? super K>> extends EntityVersionContext<K>
	{
		public EntityVersionContext<K> currentVersion(K key);
		
		public static class Default<K extends Comparable<? super K>>
			extends EntityVersionContext.Abstract<K>
			implements Mutable<K>
		{
			protected Default()
			{
				super();
			}
			
			@Override
			public EntityVersionContext<K> currentVersion(final K key)
			{
				this.key = key;
				
				return this;
			}
		}
	}
	
	public static <K extends Comparable<? super K>> AutoIncrementing<K>
		AutoIncrementing(final Function<K, K> incrementor)
	{
		return new AutoIncrementing.Default<>(incrementor);
	}
	
	public static AutoIncrementing<Long> AutoIncrementingLong()
	{
		return new AutoIncrementing.Default<>(key -> key + 1L);
	}
	
	public static AutoIncrementing<Long> AutoIncrementingSystemTimeMillis()
	{
		return new AutoIncrementing.Default<>(key -> System.currentTimeMillis());
	}
	
	public static AutoIncrementing<Long> AutoIncrementingSystemNanoTime()
	{
		return new AutoIncrementing.Default<>(key -> System.nanoTime());
	}
	
	public static AutoIncrementing<Instant> AutoIncrementingInstant()
	{
		return new AutoIncrementing.Default<>(key -> Instant.now());
	}
	
	public static interface AutoIncrementing<K extends Comparable<? super K>> extends EntityVersionContext<K>
	{
		@Override
		public K versionForUpdate();
		
		public static class Default<K extends Comparable<? super K>>
			extends EntityVersionContext.Abstract<K>
			implements AutoIncrementing<K>
		{
			private final Function<K, K> incrementor;
			
			protected Default(final Function<K, K> incrementor)
			{
				super();
				
				this.incrementor = notNull(incrementor);
			}
			
			@Override
			public K versionForUpdate()
			{
				return this.key = this.incrementor.apply(this.key);
			}
		}
	}
}
