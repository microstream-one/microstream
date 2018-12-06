package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceBuildItem.Creator;



/**
 * Type to define a local persistence context for handling entities. For example to separate the entity tracking state
 * (registry) in a usage for communiction from the global registry tracking the application's persistent entity graph.
 */
public interface PersistenceContext<M>
{
	public <I extends PersistenceBuildItem<M>> I createBuildItem(
		PersistenceBuildItem.Creator<M, I> creator ,
		long                               objectId,
		long                               typeId
	);

	/**
	 * Creates a build item with potentially missing type information.
	 * Type information that is missing at the time of the call of this method has be to determined later
	 * on demand, e.g. when newly received data meanwhile added the OID<->TID association
	 *
	 * @param creator
	 * @param objectId
	 * @return
	 */
	public <I extends PersistenceBuildItem<M>> I createBuildItem(
		PersistenceBuildItem.Creator<M, I> creator ,
		long                               objectId
	);

	public <I extends PersistenceBuildItem<M>> I createSkipBuildItem(
		PersistenceBuildItem.Creator<M, I> creator ,
		long                               objectId
	);

	public PersistenceTypeHandler<M, ?> lookupTypeHandler(long typeId);

	public PersistenceTypeHandler<M, ?> lookupTypeHandler(long objectId, long typeId);
	
	public Object optionalRegisterObject(long objectId, Object object);

	public Object lookupObject(long objectId);

	public boolean handleKnownObject(long objectId, PersistenceInstanceHandler handler);

	public Object lookupObject(long objectId, long typeId);

	/**
	 * Commits all uncommitted instances to an effective state, e.g. a parent context or global object registry.
	 * May be a no-op if no such action is applicable (e.g. global object registry is already used internally or
	 * used registry is a local stand-alone instance)
	 */
	public void commit();


	
	public static <M> PersistenceContext<M> New(
		final PersistenceObjectRegistry       registry  ,
		final PersistenceTypeHandlerLookup<M> typeLookup
	)
	{
		return new PersistenceContext.Implementation<>(
			notNull(registry)  ,
			notNull(typeLookup)
		);
	}

	public class Implementation<M> implements PersistenceContext<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// may be a relay lookup that provides special handlers providing logic
		private final PersistenceTypeHandlerLookup<M> typeLookup;
		
		// global registry to synch with other threads
		private final PersistenceObjectRegistry registry;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceObjectRegistry       registry  ,
			final PersistenceTypeHandlerLookup<M> typeLookup

		)
		{
			super();
			this.registry   = registry  ;
			this.typeLookup = typeLookup;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <I extends PersistenceBuildItem<M>> I createBuildItem(
			final Creator<M, I> creator ,
			final long          objectId
		)
		{
			return creator.createBuildItem(
				objectId,
				null,
				this.lookupObject(objectId)
			);
		}

		@SuppressWarnings("unchecked") // weird interplay between ? and Object. Correctness guaranteed by logic.
		@Override
		public <I extends PersistenceBuildItem<M>> I createBuildItem(
			final Creator<M, I> creator ,
			final long          objectId,
			final long          typeId
		)
		{
			// type handler lookup (potential miss / validation error, etc.).
			return creator.createBuildItem(
				objectId,
				(PersistenceTypeHandler<M, Object>)this.lookupTypeHandler(objectId, typeId),
				this.lookupObject(objectId)
			);
		}

		@Override
		public <I extends PersistenceBuildItem<M>> I createSkipBuildItem(
			final PersistenceBuildItem.Creator<M, I> creator ,
			final long                               objectId
		)
		{
			return creator.createSkipBuildItem(
				objectId,
				this.lookupObject(objectId)
			);
		}
		
		@Override
		public Object optionalRegisterObject(final long objectId, final Object object)
		{
			return this.registry.optionalRegisterObject(objectId, object);
		}

		@Override
		public PersistenceTypeHandler<M, ?> lookupTypeHandler(final long typeId)
		{
			return this.typeLookup.lookupTypeHandler(typeId);
		}

		@Override
		public PersistenceTypeHandler<M, ?> lookupTypeHandler(final long objectId, final long typeId)
		{
			return this.typeLookup.lookupTypeHandler(objectId, typeId);
		}

		@Override
		public Object lookupObject(final long objectId)
		{
			return this.registry.lookupObject(objectId);
		}

		@Override
		public final boolean handleKnownObject(final long objectId, final PersistenceInstanceHandler handler)
		{
			final Object instance = this.registry.lookupObject(objectId);
			if(instance == null)
			{
				return false;
			}
			
			handler.handle(objectId, instance);
			
			return true;
		}

		@Override
		public Object lookupObject(final long objectId, final long typeId)
		{
			return this.lookupObject(objectId);
		}

		@Override
		public void commit()
		{
			// no-op, see JavaDoc
		}

	}

}
