package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;


public final class BinaryHandlerPersistenceRootReferenceDefault
extends AbstractBinaryHandlerCustom<PersistenceRootReference.Default>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPersistenceRootReferenceDefault New(
		final PersistenceRootReference.Default instance      ,
		final PersistenceObjectRegistry        globalRegistry
	)
	{
		return new BinaryHandlerPersistenceRootReferenceDefault(
			mayNull(instance),
			notNull(globalRegistry)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/**
	 * The handler instance directly knowing the global registry might suprise at first and seem like a shortcut hack.
	 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
	 * it becomes clear that a direct access for registering resolved global instances at the global registry is
	 * indeed part of this handler's task.
	 */
	final PersistenceRootReference.Default instance      ;
	final PersistenceObjectRegistry        globalRegistry;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPersistenceRootReferenceDefault(
		final PersistenceRootReference.Default instance      ,
		final PersistenceObjectRegistry        globalRegistry
	)
	{
		super(
			PersistenceRootReference.Default.class,
			CustomFields(
				CustomField(Object.class, "root")
			)
		);
		this.instance       = instance      ;
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
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		final Object rootInstance = this.instance.get();
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
			idResolver.registerRoot(this.instance.get(), rootObjectId);
		}
		
		// instance is a singleton. Hence, no instance is created, here, but the singleton is returned.
		return this.instance;
	}

	@Override
	public final void update(
		final Binary                           bytes   ,
		final PersistenceRootReference.Default instance,
		final PersistenceObjectIdResolver      handler
	)
	{
		final Object rootInstance = this.instance.get();
		if(rootInstance != null)
		{
			/*
			 * If the singleton instance references a defined root object, this method is a no-op:
			 * The effective root instance is already set, there is nothing to
			 */
			return;
		}
		
		// (10.12.2019 TM)FIXME: priv#194
		final long   rootObjectId = getRootObjectId(bytes);
		final Object rootInstance = handler.lookupObject(rootObjectId);
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
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		// (10.12.2019 TM)FIXME: priv#194
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
