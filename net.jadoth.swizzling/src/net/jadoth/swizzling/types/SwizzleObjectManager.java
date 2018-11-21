package net.jadoth.swizzling.types;

import static net.jadoth.X.notNull;

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

		private final SwizzleObjectIdProvider oidProvider          ;
		private final SwizzleObjectRegistry   swizzleObjectRegistry;
		private final SwizzleTypeManager      typeManager          ;
		/*
		 * Note that the type manager may not be a tid-assigning (master) manager on a type slave side!
		 * It must be a retrieving type handler manager instead.
		 */



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final SwizzleObjectRegistry   swizzleObjectRegistry,
			final SwizzleObjectIdProvider oidProvider          ,
			final SwizzleTypeManager      typeManager
		)
		{
			super();
			this.oidProvider           = notNull(oidProvider)          ;
			this.swizzleObjectRegistry = notNull(swizzleObjectRegistry);
			this.typeManager           = notNull(typeManager)          ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void cleanUp()
		{
			this.swizzleObjectRegistry.cleanUp();
		}

		@Override
		public long lookupObjectId(final Object object)
		{
			return this.swizzleObjectRegistry.lookupObjectId(object);
		}

		@Override
		public Object lookupObject(final long oid)
		{
//			XDebug.debugln(XChars.systemString(this) + " looking up \n" + oid
//				+ " -> " + XChars.systemString(this.swizzleRegistry.lookupObject(oid))
//			);

			return this.swizzleObjectRegistry.lookupObject(oid);
		}

//		protected long internalLookupExistingTypeId(final Class<?> type)
//		{
//			final long tid;
//			if((tid = this.swizzleObjectRegistry.lookupTypeId(type)) == 0L)
//			{
//				throw new SwizzleExceptionConsistencyUnknownType(type);
//			}
//			return tid;
//		}

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

			// if not found either assign new oid or return the meanwhile registered oid
			synchronized(this.swizzleObjectRegistry)
			{
				if((oid = this.swizzleObjectRegistry.lookupObjectId(object)) == 0L)
				{
					oid = this.oidProvider.provideNextObjectId();
					this.swizzleObjectRegistry.registerObject(oid, object);
				}
			}

//			XDebug.debugln(XChars.systemString(this) + " assigned \n" + oid
//				+ " -> " + XChars.systemString(this.swizzleRegistry.lookupObject(oid))
//			);
			return oid;
		}

		@Override
		public final long currentObjectId()
		{
			synchronized(this.swizzleObjectRegistry)
			{
				return this.oidProvider.currentObjectId();
			}
		}

		@Override
		public void updateCurrentObjectId(final long currentObjectId)
		{
			synchronized(this.swizzleObjectRegistry)
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
