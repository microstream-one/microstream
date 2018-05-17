package net.jadoth.swizzling.types;

import static net.jadoth.X.notNull;

import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyUnknownTid;


public interface SwizzleTypeManager extends SwizzleTypeRegistry
{
	public long ensureTypeId(Class<?> type);

	public <T> Class<T> ensureType(long typeId);

	public long currentTypeId();

	public void updateCurrentHighestTypeId(long highestTypeId);



	public final class Implementation implements SwizzleTypeManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final SwizzleTypeRegistry   typeRegistry;
		final SwizzleTypeIdProvider tidProvider ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////


		public Implementation(final SwizzleTypeRegistry registry, final SwizzleTypeIdProvider tidProvider)
		{
			super();
			this.typeRegistry   = notNull(registry   );
			this.tidProvider    = notNull(tidProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected long createNewTid()
		{
			return this.tidProvider.provideNextTypeId();
		}

		protected long internalEnsureTypeId(final Class<?> type)
		{
			long tid;
			synchronized(this.typeRegistry)
			{
				// if not found either assign new oid or return the meanwhile registered oid
				if((tid = this.typeRegistry.lookupTypeId(type)) != 0L)
				{
					return tid;
				}
				tid = this.createNewTid();

				this.typeRegistry.registerType(tid, type);
				if(type.getSuperclass() != null)
				{
					this.ensureTypeId(type.getSuperclass());
				}
			}
			return tid;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public void validateExistingTypeMapping(final long typeId, final Class<?> type)
		{
			this.typeRegistry.validateExistingTypeMapping(typeId, type);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type)
		{
			return this.typeRegistry.registerType(tid, type);
		}

		@Override
		public void validateExistingTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeRegistry.validateExistingTypeMappings(mappings);
		}

		@Override
		public void validatePossibleTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeRegistry.validatePossibleTypeMappings(mappings);
		}

		@Override
		public long ensureTypeId(final Class<?> type)
		{
			long tid; // quick read-only check for already registered tid
			if((tid = this.typeRegistry.lookupTypeId(type)) != 0L)
			{
				return tid;
			}
//			XDebug.debugln("not yet contained type id for " + type);
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
		public <T> Class<T> ensureType(final long typeId)
		{
			Class<T> type;
			if((type = this.typeRegistry.lookupType(typeId)) == null)
			{
				throw new SwizzleExceptionConsistencyUnknownTid(typeId);
			}
			return type;
		}

	}

}
