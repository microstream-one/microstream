
package one.microstream.entity;

import static one.microstream.X.notNull;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XCollection;

/**
 * A mutable entity. Mutations of the entity's data only happen by providing another instance of that entity
 * that contains the desired new data.
 * 
 * @author TM, FH
 */
public interface Entity
{
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E identity(final E instance)
	{
		if(instance instanceof Entity.AbstractAccessible)
		{
			return (E)((Entity.AbstractAccessible)instance).entityIdentity();
		}
		
		if(instance instanceof Entity.Accessible)
		{
			return (E)((Entity.Accessible)instance).entityIdentity();
		}
		
		if(instance == null)
		{
			// null is consistently its own identity
			return null;
		}

		throw new EntityExceptionUnaccessibleEntityType(instance);
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E data(final E instance)
	{
		if(instance instanceof Entity.AbstractAccessible)
		{
			return (E)((Entity.AbstractAccessible)instance).entityData();
		}
		
		// tiny redundancy as a tiny price for convenient visibility magic plus still possible multiple inheritance
		if(instance instanceof Entity.Accessible)
		{
			return (E)((Entity.Accessible)instance).entityData();
		}
		
		if(instance == null)
		{
			// null is consistently its own data
			return null;
		}

		throw new EntityExceptionUnaccessibleEntityType(instance);
	}
	
	public static <E extends Entity> boolean updateData(final E entity, final E data)
	{
		if(entity instanceof Entity.AbstractAccessible)
		{
			// data instance validation is done inside (has to be, anyway)
			return ((Entity.AbstractAccessible)entity).updateEntityData(data);
		}
		
		// tiny redundancy as a tiny price for convenient visibility magic plus still possible multiple inheritance
		if(entity instanceof Entity.Accessible)
		{
			// data instance validation is done inside (has to be, anyway)
			return ((Entity.Accessible)entity).updateEntityData(data);
		}
		
		throw new EntityExceptionUnaccessibleEntityType(entity);
	}
	
	public default boolean isSameIdentity(final Entity other)
	{
		return identity(this) == identity(other);
	}
	
	public default void validateIdentity(final Entity newData)
	{
		if(this.isSameIdentity(newData))
		{
			return;
		}
		
		throw new EntityExceptionIdentityMismatch(this, newData);
	}
	
	/**
	 * Primary means to convenience-hide framework-internal methods from the user entity's public API
	 * 
	 * @author TM
	 */
	public abstract class AbstractAccessible implements Entity
	{
		protected abstract Entity entityIdentity();
		
		protected abstract Entity entityData();
		
		protected abstract void entityCreated();
		
		protected abstract boolean updateEntityData(Entity data);
	}
	
	/**
	 * Fallback means to convenience-hide framework-internal methods from the user entity's public API
	 * for classes that cannot extend {@link AbstractAccessible} for whatever reason
	 * 
	 * @author TM
	 */
	public interface Accessible extends Entity
	{
		public Entity entityIdentity();
		
		public Entity entityData();
		
		public void entityCreated();
		
		public boolean updateEntityData(Entity data);
	}
	
	public interface Creator<E extends Entity, C extends Creator<E, C>>
	{
		public E create();
		
		public E createData(E identityInstance);
		
		public C copy(E other);
		
		@SuppressWarnings("unchecked")
		public default C addLayer(final EntityLayerProvider layerProvider)
		{
			synchronized(this)
			{
				final XCollection<EntityLayerProvider> layerProviders = this.layers();
				synchronized(layerProviders)
				{
					layerProviders.add(layerProvider);
				}
			}
			
			return (C)this;
		}
		
		public default C addLayer(final EntityLayerProviderProvider layerProviderProvider)
		{
			return this.addLayer(layerProviderProvider.provideEntityLayerProvider());
		}
		
		public XCollection<EntityLayerProvider> layers();
		
		public abstract class Abstract<E extends Entity, C extends Creator<E, C>>
			implements Entity.Creator<E, C>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final BulkList<EntityLayerProvider> layerProviders = BulkList.New();
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public XCollection<EntityLayerProvider> layers()
			{
				return this.layerProviders;
			}
			
			protected Entity dispatchDataInstance(final Entity dataInstance)
			{
				Entity innerLayer = dataInstance;
				for(final EntityLayerProvider lp : this.layerProviders)
				{
					innerLayer = lp.provideEntityLayer(innerLayer);
				}
				
				return innerLayer;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public E create()
			{
				final EntityLayerIdentity entity        = this.createEntityInstance();
				
				final Entity              data          = this.createData((E)entity.entityIdentity());
				final Entity              innerInstance = this.dispatchDataInstance(data);
				
				entity.setInner(innerInstance);
				
				Static.entityCreated(entity);
				
				return (E)entity.entityIdentity();
			}
			
			protected abstract EntityLayerIdentity createEntityInstance();
			
		}
		
		static class Static
		{
			static void entityCreated(final Entity entity)
			{
				if(entity instanceof Entity.AbstractAccessible)
				{
					((Entity.AbstractAccessible)entity).entityCreated();
				}
				else if(entity instanceof Entity.Accessible)
				{
					((Entity.Accessible)entity).entityCreated();
				}
			}
		}
	}
	
	public interface Updater<E extends Entity, U extends Updater<E, U>>
	{
		public boolean update();
		
		public U copy(E data);
		
		public static abstract class Abstract<E extends Entity, U extends Updater<E, U>>
			implements Updater<E, U>
		{
			private final E entityIdentity;
			
			protected Abstract(final E entity)
			{
				super();
				this.entityIdentity = Entity.identity(notNull(entity));
				this.copy(Entity.data(entity));
			}
			
			protected abstract E createData(E identityInstance);
			
			@Override
			public boolean update()
			{
				return Entity.updateData(this.entityIdentity, this.createData(this.entityIdentity));
			}
		}
	}
	
}
