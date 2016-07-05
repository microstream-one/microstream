package net.jadoth.swizzling.types;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyUnknownType;

public interface SwizzleObjectManager extends SwizzleObjectLookup
{
	public long ensureObjectId(Object object);

	public void cleanUp();

	public long currentObjectId();

	public void updateCurrentObjectId(long currentObjectId);



	public class Implementation implements SwizzleObjectManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final SwizzleObjectIdProvider oidProvider    ;
		private final SwizzleRegistry         swizzleRegistry;
		private final SwizzleTypeManager      typeManager    ;
		/*
		 * Note that the type manager may not be a tid-assigning (master) manager on a type slave side!
		 * It must be a retrieving type handler manager instead.
		 */



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final SwizzleRegistry         swizzleRegistry,
			final SwizzleObjectIdProvider oidProvider    ,
			final SwizzleTypeManager      typeManager
		)
		{
			super();
			this.oidProvider     = notNull(oidProvider)    ;
			this.swizzleRegistry = notNull(swizzleRegistry);
			this.typeManager     = notNull(typeManager)    ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public void cleanUp()
		{
			this.swizzleRegistry.cleanUp();
		}

		@Override
		public long lookupObjectId(final Object object)
		{
			return this.swizzleRegistry.lookupObjectId(object);
		}

		@Override
		public Object lookupObject(final long oid)
		{
//			JadothConsole.debugln(Jadoth.systemString(this) + " looking up \n" + oid
//				+ " -> " + Jadoth.systemString(this.swizzleRegistry.lookupObject(oid))
//			);

			return this.swizzleRegistry.lookupObject(oid);
		}

		protected long internalLookupExistingTypeId(final Class<?> type)
		{
			final long tid;
			if((tid = this.swizzleRegistry.lookupTypeId(type)) == 0L)
			{
				throw new SwizzleExceptionConsistencyUnknownType(type);
			}
			return tid;
		}

		@Override
		public long ensureObjectId(final Object object)
		{
			long oid; // quick read-only check for already registered oid
			if((oid = this.lookupObjectId(object)) != 0L)
			{
				return oid;
			}

			// Class instances can be passed here as well if they are normally referenced in an object graph.
			if(object instanceof Class<?>)
			{
				return this.typeManager.ensureTypeId((Class<?>)object);
			}

			final long tid = this.typeManager.ensureTypeId(object.getClass());

			// if not found either assign new oid or return the meanwhile registered oid
			synchronized(this.swizzleRegistry)
			{
				if((oid = this.swizzleRegistry.lookupObjectId(object)) == 0L)
				{
					oid = this.oidProvider.provideNextObjectId();
					this.swizzleRegistry.registerObject(oid, tid, object);
				}
			}

//			JadothConsole.debugln(Jadoth.systemString(this) + " assigned \n" + oid
//				+ " -> " + Jadoth.systemString(this.swizzleRegistry.lookupObject(oid))
//			);
			return oid;
		}

		@Override
		public final long currentObjectId()
		{
			synchronized(this.swizzleRegistry)
			{
				return this.oidProvider.currentObjectId();
			}
		}

		@Override
		public void updateCurrentObjectId(final long currentObjectId)
		{
			synchronized(this.swizzleRegistry)
			{
				if(this.oidProvider.currentObjectId() >= currentObjectId)
				{
					return;
				}
				this.oidProvider.updateCurrentObjectId(currentObjectId);
			}
		}

	}

}
