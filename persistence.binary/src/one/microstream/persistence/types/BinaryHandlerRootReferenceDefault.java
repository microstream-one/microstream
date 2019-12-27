package one.microstream.persistence.types;

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;

public final class BinaryHandlerRootReferenceDefault extends AbstractBinaryHandlerCustom<PersistenceRootReference.Default>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/**
	 * The handler instance directly knowing the global registry might suprise at first and seem like a shortcut hack.
	 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
	 * it becomes clear that a direct access for registering resolved global instances at the global registry is
	 * indeed part of this handler's task.
	 */
	final PersistenceRootReference.Default rootReference ;
	final PersistenceObjectRegistry        globalRegistry;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerRootReferenceDefault(
		final PersistenceRootReference.Default rootReference ,
		final PersistenceObjectRegistry        globalRegistry
		)
	{
		super(
			PersistenceRootReference.Default.class,
			CustomFields(
				CustomField(Object.class, "root")
			)
		);
		this.rootReference  = rootReference ;
		this.globalRegistry = globalRegistry;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	static long getRootObjectId(final Binary bytes)
	{
		return bytes.read_long(0);
	}

	@Override
	public final void store(
		final Binary                           bytes   ,
		final PersistenceRootReference.Default instance,
		final long                             objectId,
		final PersistenceStoreHandler          handler
		)
	{
		// root instance may even be null. Probably just temporarily to "truncate" a database or something like that.
		final Object rootInstance  = instance.get()             ;
		final long   contentLength = Binary.objectIdByteLength();
		final long   rootObjectId  = handler.apply(rootInstance);
		bytes.storeEntityHeader(contentLength, this.typeId(), objectId);
		bytes.store_long(rootObjectId);
	}

	@Override
	public final PersistenceRootReference.Default create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
		)
	{
		final Object rootInstance = this.rootReference.get();
		if(rootInstance != null)
		{
			/*
			 * If the singleton instance references a defined root object, it must be registered for the persisted
			 * objectId, even if the id references a record of different, incompatible, type. This conflict
			 * has to be recognized and reported in the corresponding type handler, but the defined root instance
			 * must have the persisted root object id associated in any case. Otherwise, there would be an
			 * inconsistency: a generic instance would be created for the persisted record and be generically
			 * registered with the persistetd object id, thus leaving no object id for the actually defined root
			 * instance to be registered/associated with.
			 * If no rootInstance is defined, there is no such conflict. The generic instance of whatever type
			 * gets created and registered and can be queried by the application logic after initialization is
			 * complete.
			 */
			final long rootObjectId = getRootObjectId(bytes);
			handler.requireRoot(rootInstance, rootObjectId);
		}

		// instance is a singleton. Hence, no instance is created, here, but the singleton is returned.
		return this.rootReference;
	}

	@Override
	public final void update(
		final Binary                           bytes   ,
		final PersistenceRootReference.Default instance,
		final PersistenceLoadHandler      handler
	)
	{
		if(instance != this.rootReference)
		{
			// (20.12.2019 TM)EXCP: proper exception
			throw new PersistenceException(
				"Initialized root reference and loaded root reference are not the same: initialized = "
				+ XChars.systemString(this.rootReference)
				+ " <-> loaded = "
				+ XChars.systemString(instance)
			);
		}
		
		final long   rootObjectId = getRootObjectId(bytes);
		final Object loadedRoot   = handler.lookupObject(rootObjectId);
		
		final Object rootInstance = this.rootReference.get();
		if(rootInstance == null)
		{
			/*
			 * If the instance has no explicit root instance set, a
			 * generically loaded and instantiated root instance is set.
			 */
			this.rootReference.setRoot(loadedRoot);

			return;
		}
		
		if(rootInstance == loadedRoot)
		{
			/*
			 * If referenced and loaded root are the same, everything is fine. No-op at this point.
			 * (#create should already have ensured that this case applies, but who knows ...)
			 */
			return;
		}
		
		// (20.12.2019 TM)EXCP: proper exception
		throw new PersistenceException(
			"Initialized root instance and loaded root instance are not the same: initialized = "
			+ XChars.systemString(rootInstance)
			+ " <-> loaded = "
			+ XChars.systemString(loadedRoot)
		);
	}

	@Override
	public final void iterateInstanceReferences(
		final PersistenceRootReference.Default instance,
		final PersistenceFunction              iterator
		)
	{
		instance.iterate(iterator);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceReferenceLoader iterator)
	{
		// trivial single-reference
		final long rootObjectId = getRootObjectId(bytes);
		
		// must require reference eagerly here as the call in #create did not create a build item.
		iterator.requireReferenceEager(rootObjectId);
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}
			
}