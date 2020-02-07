package one.microstream.storage.restadapter;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.Persistence.IdType;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageRawFileStatistics;

public class EmbeddedStorageRestAdapter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ViewerBinaryPersistenceManager viewerPersistenceManager;
	private final EmbeddedStorageManager embeddedStorageManager;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public EmbeddedStorageRestAdapter(final EmbeddedStorageManager storage)
	{
		this.viewerPersistenceManager = new ViewerBinaryPersistenceManager(storage);
		this.embeddedStorageManager = storage;
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
				throw new ViewerException(e.getCause().getMessage());
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
	public ObjectDescription getConstant(final long objectId)
	{
		return this.viewerPersistenceManager.getStorageConstant(objectId);
	}

	/**
	 * Get all registered root elements of the current microstream instance
	 *
	 * @return List of ViewerRootDescription objects
	 */
	public List<ViewerRootDescription> getRoots()
	{
		final PersistenceObjectRegistry registry = this.embeddedStorageManager.persistenceManager().objectRegistry();
		final PersistenceRootsView roots = this.embeddedStorageManager.viewRoots();

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
	public ViewerRootDescription getRoot()
	{
		final PersistenceObjectRegistry registry = this.embeddedStorageManager.persistenceManager().objectRegistry();
		final PersistenceRootsView roots = this.embeddedStorageManager.viewRoots();

		final Object defaultRoot = roots.rootReference().get();
		if(defaultRoot != null)
		{
			return new ViewerRootDescription(PersistenceRootsView.rootIdentifier(), registry.lookupObjectId(defaultRoot));
		}

		return new ViewerRootDescription(PersistenceRootsView.rootIdentifier(), 0);
	}

	public String getTypeDictionary()
	{
		final PersistenceTypeDictionaryAssembler assembler = PersistenceTypeDictionaryAssembler.New();
		return assembler.assemble(this.embeddedStorageManager.typeDictionary());
	}

	public StorageRawFileStatistics getFileStatistics()
	{
		return this.embeddedStorageManager.createStorageStatistics();
	}
}
