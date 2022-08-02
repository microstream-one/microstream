package one.microstream.storage.embedded.types;

import one.microstream.persistence.types.ObjectIdsProcessor;
import one.microstream.persistence.types.ObjectIdsSelector;
import one.microstream.persistence.types.PersistenceObjectRegistry;

public interface EmbeddedStorageObjectRegistryCallback extends ObjectIdsSelector
{
	public void initializeObjectRegistry(PersistenceObjectRegistry objectRegistry);



	public static EmbeddedStorageObjectRegistryCallback New()
	{
		return new EmbeddedStorageObjectRegistryCallback.Default();
	}

	public final class Default implements EmbeddedStorageObjectRegistryCallback
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private PersistenceObjectRegistry objectRegistry;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized void initializeObjectRegistry(final PersistenceObjectRegistry objectRegistry)
		{
			if(this.objectRegistry != null)
			{
				if(this.objectRegistry == objectRegistry)
				{
					return;
				}

				// (29.07.2022 TM)EXCP: proper exception
				throw new RuntimeException("ObjectRegistry already initialized.");
			}

			this.objectRegistry = objectRegistry;
		}

		@Override
		public synchronized void processSelected(final ObjectIdsProcessor processor)
		{
			if(this.objectRegistry == null)
			{
				// object registry not yet initialized (i.e. no application-side storage connection yet)
				processor.processObjectIdsByFilter(objectId -> false);
				return;
			}

			// efficient for embedded mode, but servermode should use #selectLiveObjectIds instead.
			this.objectRegistry.processLiveObjectIds(processor);
		}

	}

}
