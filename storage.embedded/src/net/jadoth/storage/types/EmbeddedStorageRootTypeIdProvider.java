package net.jadoth.storage.types;

import static net.jadoth.math.XMath.positive;

import net.jadoth.persistence.types.PersistenceTypeManager;

public interface EmbeddedStorageRootTypeIdProvider extends StorageRootTypeIdProvider
{
	public void initialize(PersistenceTypeManager typeIdResolver);



	public static EmbeddedStorageRootTypeIdProvider New(final Class<?> rootType)
	{
		return new EmbeddedStorageRootTypeIdProvider.Implementation(rootType);
	}

	public final class Implementation implements EmbeddedStorageRootTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final Class<?> rootType;

		private transient Long cachedRootTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final Class<?> rootType)
		{
			super();
			this.rootType = rootType;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideRootTypeId()
		{
			if(this.cachedRootTypeId == null)
			{
				// (20.05.2013)EXCP: proper exception
				throw new IllegalStateException("not initialized");
			}
			return this.cachedRootTypeId;
		}

		@Override
		public final void initialize(final PersistenceTypeManager typeIdResolver)
		{
			final long typeId = typeIdResolver.ensureTypeId(this.rootType);
			this.cachedRootTypeId = positive(typeId);
		}

	}

}
