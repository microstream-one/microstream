package one.microstream.storage.restadapter.types;

import static one.microstream.X.notNull;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.Persistence.IdType;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.restadapter.exceptions.StorageRestAdapterException;
import one.microstream.storage.types.StorageManager;
import one.microstream.storage.types.StorageRawFileStatistics;

public interface EmbeddedStorageRestAdapter
{
	public ObjectDescription getStorageObject(long objectId);

	public ObjectDescription getConstant(long objectId);

	public List<ViewerRootDescription> getRoots();

	public ViewerRootDescription getRoot();

	public String getTypeDictionary();

	public StorageRawFileStatistics getFileStatistics();


	public static EmbeddedStorageRestAdapter New(final StorageManager storage)
	{
		notNull(storage);

		return new Default(
			ViewerBinaryPersistenceManager.New(storage),
			storage
		);
	}


	public static class Default implements EmbeddedStorageRestAdapter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ViewerBinaryPersistenceManager viewerPersistenceManager;
		private final StorageManager                 storageManager;

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ViewerBinaryPersistenceManager viewerPersistenceManager,
			final StorageManager                 storageManager
		)
		{
			this.viewerPersistenceManager = viewerPersistenceManager;
			this.storageManager           = storageManager;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		/**
		 *
		 * Get an object's description by a microstream ObjectId
		 *
		 * @param objectId
		 * @return ViewerObjectDescription
		 */
		@Override
		public ObjectDescription getStorageObject(final long objectId)
		{

			final IdType idType = IdType.determineFromValue(objectId);

			if(idType == IdType.CID)
			{
				return this.getConstant(objectId);
			}

			try
			{
				return this.viewerPersistenceManager.getStorageObject(objectId);
			}
			//TODO will be a StorageException soon ...
			catch(final RuntimeException e)
			{
				if(e.getCause() instanceof StorageException)
				{
					throw new StorageRestAdapterException(e.getCause().getMessage());
				}
				throw e;
			}
		}

		/**
		 *
		 * Get java constants values
		 *
		 * @param objectId
		 * @return the constants value as object
		 */
		@Override
		public ObjectDescription getConstant(final long objectId)
		{
			return this.viewerPersistenceManager.getStorageConstant(objectId);
		}

		/**
		 * Get all registered root elements of the current microstream instance
		 *
		 * @return List of ViewerRootDescription objects
		 */
		@Override
		public List<ViewerRootDescription> getRoots()
		{
			final PersistenceObjectRegistry registry = this.storageManager.persistenceManager().objectRegistry();
			final PersistenceRootsView roots = this.storageManager.viewRoots();

			final List<ViewerRootDescription> rootDescriptions = new ArrayList<>();

			roots.iterateEntries((id, root) ->
			{
				rootDescriptions.add(new ViewerRootDescription(id, registry.lookupObjectId(root)));
			});

			return rootDescriptions;
		}

		/**
		 * Get the current root name and object id
		 *
		 * If no default root is registered the returned ViewerRootDescription
		 * will have a "null" string as name and objectId 0.
		 *
		 * @return ViewerRootDescription
		 */
		@Override
		public ViewerRootDescription getRoot()
		{
			final PersistenceObjectRegistry registry = this.storageManager.persistenceManager().objectRegistry();
			final PersistenceRootsView roots = this.storageManager.viewRoots();

			final Object defaultRoot = roots.rootReference().get();
			if(defaultRoot != null)
			{
				return new ViewerRootDescription(PersistenceRootsView.rootIdentifier(), registry.lookupObjectId(defaultRoot));
			}

			return new ViewerRootDescription(PersistenceRootsView.rootIdentifier(), 0);
		}

		@Override
		public String getTypeDictionary()
		{
			final PersistenceTypeDictionaryAssembler assembler = PersistenceTypeDictionaryAssembler.New();
			return assembler.assemble(this.storageManager.typeDictionary());
		}

		@Override
		public StorageRawFileStatistics getFileStatistics()
		{
			return this.storageManager.createStorageStatistics();
		}

	}

}
