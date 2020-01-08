package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.binary.types.ViewerBinaryPersistenceManager;
import one.microstream.persistence.binary.types.ViewerException;
import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.types.EmbeddedStorageManager;

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
		this.viewerPersistenceManager = new ViewerBinaryPersistenceManager(storage.persistenceManager());
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
	public ViewerObjectDescription getStorageObject(final long objectId)
	{
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
	public Object getConstant(final long objectId)
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
			//return new ViewerRootDescription(roots.rootIdentifier(), registry.lookupObjectId(defaultRoot));
			return new ViewerRootDescription(Persistence.rootIdentifier(), registry.lookupObjectId(defaultRoot));
		}

		//return new ViewerRootDescription(roots.rootIdentifier(), 0);
		return new ViewerRootDescription(Persistence.rootIdentifier(), 0);
	}

	/**
	 * Get the current default root name and object id
	 *
	 * If no default root is registered the returned ViewerRootDescription
	 * will have a "null" string as name and objectId 0.
	 *
	 * @return ViewerRootDescription
	 */
//	public ViewerRootDescription getDefaultRoot()
//	{
//		final PersistenceObjectRegistry registry = this.embeddedStorageManager.persistenceManager().objectRegistry();
//		final PersistenceRootsView roots = this.embeddedStorageManager.viewRoots();
//
//		final Object defaultRoot = roots.defaultRoot();
//		if(defaultRoot != null)
//		{
//			return new ViewerRootDescription(roots.defaultRootIdentifier(), registry.lookupObjectId(defaultRoot));
//		}
//
//		return new ViewerRootDescription(roots.defaultRootIdentifier(), 0);
//	}

	/**
	 * Get the current custom root name and object id
	 *
	 * If no custom root is registered the returned ViewerRootDescription
	 * will have a "null" string as name and objectId 0.
	 *
	 * @return ViewerRootDescription
	 */
//	public ViewerRootDescription getCustomRoot()
//	{
//		final PersistenceObjectRegistry registry = this.embeddedStorageManager.persistenceManager().objectRegistry();
//		final PersistenceRootsView roots = this.embeddedStorageManager.viewRoots();
//
//		final Object customRoot = roots.customRoot();
//		if(customRoot != null)
//		{
//			return new ViewerRootDescription(roots.customRootIdentifier(), registry.lookupObjectId(customRoot));
//		}
//
//		return new ViewerRootDescription(roots.customRootIdentifier(), 0);
//	}


	/**
	 * Get the current (either Custom or Default) root name and object id
	 *
	 * If no default or custom root is registered the returned ViewerRootDescription
	 * will have a "null" string as name and objectId 0.
	 *
	 * @return ViewerRootDescription
	 */
//	public ViewerRootDescription getUserRoot()
//	{
//		final PersistenceObjectRegistry registry = this.embeddedStorageManager.persistenceManager().objectRegistry();
//		final PersistenceRootsView roots = this.embeddedStorageManager.viewRoots();
//
//		final Object customRoot = roots.customRoot();
//		if(customRoot != null)
//		{
//			return new ViewerRootDescription(roots.customRootIdentifier(), registry.lookupObjectId(customRoot));
//		}
//
//		final Object defaultRoot = roots.defaultRoot();
//		if(defaultRoot != null)
//		{
//			return new ViewerRootDescription(roots.defaultRootIdentifier(), registry.lookupObjectId(defaultRoot));
//		}
//
//
//		return new ViewerRootDescription(roots.customRootIdentifier(), 0);
//	}

	/**
	 * get the ObjectId of the current root object
	 *
	 * @return > 0 if ObjectId has been found, otherwise 0
	 */
//	public long getRootId()
//	{
//		final PersistenceObjectRegistry registry = this.embeddedStorageManager.persistenceManager().objectRegistry();
//		final PersistenceRootsView roots = this.embeddedStorageManager.viewRoots();
//
//		Object rootObject = roots.defaultRoot();
//		if(rootObject == null)
//		{
//			rootObject = roots.customRoot();
//		}
//
//		if(rootObject != null)
//		{
//			return registry.lookupObjectId(rootObject);
//		}
//
//		return 0;
//	}
}
