package one.microstream.storage.types;

import static one.microstream.math.XMath.positive;

import one.microstream.persistence.types.PersistenceTypeManager;
import one.microstream.storage.types.StorageRootTypeIdProvider;

public interface EmbeddedStorageRootTypeIdProvider extends StorageRootTypeIdProvider
{
	public void initialize(PersistenceTypeManager typeIdResolver);



	public static EmbeddedStorageRootTypeIdProvider New(final Class<?> rootType)
	{
		return new EmbeddedStorageRootTypeIdProvider.Default(rootType);
	}

	public final class Default implements EmbeddedStorageRootTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Class<?> rootType;

		private transient Long cachedRootTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Class<?> rootType)
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
				// (20.05.2013 TM)EXCP: proper exception
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
