package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

public interface PersistenceObjectManager extends PersistenceObjectLookup
{
	public long ensureObjectId(Object object);

	public void cleanUp();

	public long currentObjectId();

	public void updateCurrentObjectId(long currentObjectId);



	public class Implementation implements PersistenceObjectManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceObjectIdProvider oidProvider   ;
		private final PersistenceObjectRegistry   objectRegistry;
		/*
		 * Note that the type manager may not be a tid-assigning (master) manager on a type slave side!
		 * It must be a retrieving type handler manager instead.
		 */



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceObjectRegistry   objectRegistry,
			final PersistenceObjectIdProvider oidProvider
		)
		{
			super();
			this.oidProvider    = notNull(oidProvider)   ;
			this.objectRegistry = notNull(objectRegistry);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void cleanUp()
		{
			this.objectRegistry.cleanUp();
		}

		@Override
		public long lookupObjectId(final Object object)
		{
			return this.objectRegistry.lookupObjectId(object);
		}

		@Override
		public Object lookupObject(final long oid)
		{
//			XDebug.debugln(XChars.systemString(this) + " looking up \n" + oid
//				+ " -> " + XChars.systemString(this.objectRegistry.lookupObject(oid))
//			);

			return this.objectRegistry.lookupObject(oid);
		}

//		protected long internalLookupExistingTypeId(final Class<?> type)
//		{
//			final long tid;
//			if((tid = this.objectRegistry.lookupTypeId(type)) == 0L)
//			{
//				throw new PersistenceExceptionConsistencyUnknownType(type);
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
				/* (23.11.2018 TM)FIXME: proper check for invalid types
				 * There are more invalid types than just Classes.
				 * All have to be checked, probably by consolidating this implementation with the already
				 * existing registry for invalid types.
				 * Or the check has to be done in the calling context, documented by a comment here.
				 */
				
				// (23.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Invalid Class metadata instance: " + object.toString());
			}

			// if not found either assign new oid or return the meanwhile registered oid
			synchronized(this.objectRegistry)
			{
				if((oid = this.objectRegistry.lookupObjectId(object)) == 0L)
				{
					oid = this.oidProvider.provideNextObjectId();
					this.objectRegistry.registerObject(oid, object);
				}
			}

//			XDebug.debugln(XChars.systemString(this) + " assigned \n" + oid
//				+ " -> " + XChars.systemString(this.objectRegistry.lookupObject(oid))
//			);
			return oid;
		}

		@Override
		public final long currentObjectId()
		{
			synchronized(this.objectRegistry)
			{
				return this.oidProvider.currentObjectId();
			}
		}

		@Override
		public void updateCurrentObjectId(final long currentObjectId)
		{
			synchronized(this.objectRegistry)
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
