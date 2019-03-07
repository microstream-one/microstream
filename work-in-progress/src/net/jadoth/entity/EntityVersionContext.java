package net.jadoth.entity;

import net.jadoth.hashing.HashEqualator;
import net.jadoth.hashing.XHashing;

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
	
	
	
	public static <K> EntityVersionContext.Implementation<K> New()
	{
		return new EntityVersionContext.Implementation<>();
	}
	
	public final class Implementation<K> implements EntityVersionContext<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private K key;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
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
		public synchronized EntityVersionContext.Implementation<K> currentVersion(final K key)
		{
			this.key = key;
			
			return this;
		}
		
	}
}
