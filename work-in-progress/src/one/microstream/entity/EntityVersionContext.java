package one.microstream.entity;

import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;

public interface EntityVersionContext<K> extends EntityLayerProviderProvider
{
	public K currentVersion();
	
	public EntityVersionContext<K> currentVersion(K key);
	
	public default HashEqualator<? super K> equality()
	{
		return XHashing.hashEqualityValue();
	}
	
	@Override
	public default <E extends Entity<E>> EntityLayerProvider<E> provideEntityLayerProvider()
	{
		return e ->
			new EntityLayerVersioning<>(e, this)
		;
	}
	
	
	
	public static <K> EntityVersionContext.Default<K> New()
	{
		return new EntityVersionContext.Default<>();
	}
	
	public final class Default<K> implements EntityVersionContext<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private K key;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized K currentVersion()
		{
			return this.key;
		}

		@Override
		public synchronized EntityVersionContext.Default<K> currentVersion(final K key)
		{
			this.key = key;
			
			return this;
		}
		
	}
}
