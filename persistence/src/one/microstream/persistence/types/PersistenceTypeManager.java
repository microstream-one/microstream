package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyUnknownTid;


public interface PersistenceTypeManager extends PersistenceTypeRegistry
{
	public long ensureTypeId(Class<?> type);

	public Class<?> ensureType(long typeId);

	public long currentTypeId();

	public void updateCurrentHighestTypeId(long highestTypeId);

	
	
	public static PersistenceTypeManager.Implementation New(
		final PersistenceTypeRegistry   registry   ,
		final PersistenceTypeIdProvider tidProvider
	)
	{
		return new PersistenceTypeManager.Implementation(
			notNull(registry)   ,
			notNull(tidProvider)
		);
	}

	public final class Implementation implements PersistenceTypeManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeRegistry   typeRegistry;
		final PersistenceTypeIdProvider tidProvider ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final PersistenceTypeRegistry   registry   ,
			final PersistenceTypeIdProvider tidProvider
		)
		{
			super();
			this.typeRegistry = notNull(registry)   ;
			this.tidProvider  = notNull(tidProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected long createNewTypeId()
		{
			return this.tidProvider.provideNextTypeId();
		}

		protected long internalEnsureTypeId(final Class<?> type)
		{
			long tid;
			synchronized(this.typeRegistry)
			{
				// if not found either assign new oid or return the meanwhile registered oid
				if((tid = this.typeRegistry.lookupTypeId(type)) != Persistence.nullId())
				{
					return tid;
				}
				tid = this.createNewTypeId();

				this.typeRegistry.registerType(tid, type);
				if(type.getSuperclass() != null)
				{
					this.ensureTypeId(type.getSuperclass());
				}
			}
			return tid;
		}

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.typeRegistry.lookupTypeId(type);
		}

		@Override
		public <T> Class<T> lookupType(final long tid)
		{
			return this.typeRegistry.lookupType(tid);
		}
		
		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMapping(typeId, type);
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMappings(mappings);
		}
		
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerTypes(types);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type)
		{
			return this.typeRegistry.registerType(tid, type);
		}

		@Override
		public long ensureTypeId(final Class<?> type)
		{
			long tid; // quick read-only check for already registered tid
			if((tid = this.typeRegistry.lookupTypeId(type)) != Persistence.nullId())
			{
				return tid;
			}

			return this.internalEnsureTypeId(type);
		}

		@Override
		public final long currentTypeId()
		{
			synchronized(this.typeRegistry)
			{
				return this.tidProvider.currentTypeId();
			}
		}

		@Override
		public void updateCurrentHighestTypeId(final long highestTypeId)
		{
			synchronized(this.typeRegistry)
			{
				final long currentTypeId = this.tidProvider.currentTypeId();
				if(currentTypeId > highestTypeId)
				{
					throw new IllegalArgumentException(
						"Current highest type id already passed desired new highest type id: "
						+ currentTypeId + " > " + highestTypeId
					);
				}
				this.tidProvider.updateCurrentTypeId(highestTypeId);
			}
		}

		@Override
		public Class<?> ensureType(final long typeId)
		{
			Class<?> type;
			if((type = this.typeRegistry.lookupType(typeId)) == null)
			{
				throw new PersistenceExceptionConsistencyUnknownTid(typeId);
			}
			return type;
		}

	}

}
