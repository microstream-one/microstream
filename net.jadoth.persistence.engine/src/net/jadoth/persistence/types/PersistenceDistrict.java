package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceBuildItem.Creator;
import net.jadoth.swizzling.types.SwizzleRegistry;



/* Note from Idea:
 * (06.10.2012): PersistenceDistrict
 * How about bundling Registry and TypeHandlerLookup together in a kind of district instance?
 * The two are closely related in practice because local (e.g. thread-local) participants of a
 * persistence network may for example only create/update instances in a local registry,
 * while in a global registry, instances my only be validated instead of actually updated.
 * So the global registry would need another closely related set of type handlers than a local
 * "do-whatever-i-like"-registry.
 * The bundle type could also provide means to synchronize all/selected instances from one to another
 * (e.g. to validate&merge from local into global context).
 */
public interface PersistenceDistrict<M>
{
	public <I extends PersistenceBuildItem<M>> I createBuildItem(
		PersistenceBuildItem.Creator<M, I> creator,
		long oid,
		long tid
	);

	/**
	 * Creates a build item with potentially missing type information.
	 * Type information that is missing at the time of the call of this method has be to determined later
	 * on demand, e.g. when newly received data meanwhile added the OID<->TID association
	 *
	 * @param creator
	 * @param oid
	 * @return
	 */
	public <I extends PersistenceBuildItem<M>> I createBuildItem(
		PersistenceBuildItem.Creator<M, I> creator,
		long oid
	);

	public <I extends PersistenceBuildItem<M>> I createSkipBuildItem(
		PersistenceBuildItem.Creator<M, I> creator,
		long oid
	);

//	public long lookupTypeId(Class<?> type);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long typeId);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long objectId, long typeId);

	/* (23.05.2018 TM)TODO: SwizzleRegistry nonsense method?
	 * Isn't this method nonsense since the tid got removed?
	 * Wouldn't a simple lookup suffice for the calling site and the actual registration only done when it's needed?
	 * 
	 */
	public Object registerObjectId(long oid);
	
	public Object optionalRegisterObject(long oid, Object object);

	public Object lookupObject(long oid);

//	public boolean knowsReferencedObject(long referenceOid);

	public boolean handleKnownObject(long objectId, PersistenceInstanceHandler handler);

	public Object lookupObject(long oid, long tid);

	/**
	 * Commits all uncommitted instances to an effective state, e.g. a parent district or global swizzle registry.
	 * May be a no-op if no such action is applicable (e.g. global swizzle registry is already used internally or
	 * used registry is a local stand-alone instance)
	 */
	public void commit();



	public class Implementation<M> implements PersistenceDistrict<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// may be a relay lookup that provides special handlers with filtering
		private final PersistenceTypeHandlerLookup<M> typeLookup;
		
		// global registry to synch with other threads
		private final SwizzleRegistry                 registry  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final SwizzleRegistry                 registry  ,
			final PersistenceTypeHandlerLookup<M> typeLookup

		)
		{
			super();
			this.registry   = notNull(registry  );
			this.typeLookup = notNull(typeLookup);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <I extends PersistenceBuildItem<M>> I createBuildItem(final Creator<M, I> creator, final long oid)
		{
			return creator.createBuildItem(
				oid,
				null,
				this.lookupObject(oid)
			);
		}

		@Override
		public <I extends PersistenceBuildItem<M>> I createSkipBuildItem(
			final PersistenceBuildItem.Creator<M, I> creator,
			final long oid
		)
		{
			return creator.createSkipBuildItem(
				oid,
				this.lookupObject(oid)
			);
		}

		@Override
		public <I extends PersistenceBuildItem<M>> I createBuildItem(
			final Creator<M, I> creator,
			final long          oid    ,
			final long          tid
		)
		{
			// type handler lookup (potential miss / validation error, etc.) must be executed BEFORE tid registration
			return creator.createBuildItem(
				oid,
				this.lookupTypeHandler(oid, tid),
				this.registerObjectId(oid)
			);
		}
		
		@Override
		public Object registerObjectId(final long oid)
		{
			return this.registry.registerObjectId(oid);
		}
		
		@Override
		public Object optionalRegisterObject(final long oid, final Object object)
		{
			return this.registry.optionalRegisterObject(oid, object);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long typeId)
		{
			return this.typeLookup.lookupTypeHandler(typeId);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long objectId, final long typeId)
		{
			return this.typeLookup.lookupTypeHandler(objectId, typeId);
		}

		@Override
		public Object lookupObject(final long oid)
		{
			return this.registry.lookupObject(oid);
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
		public Object lookupObject(final long oid, final long tid)
		{
			return this.lookupObject(oid);
		}

		@Override
		public void commit()
		{
			// no-op, see JavaDoc
		}

	}

}
