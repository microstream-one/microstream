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

	@Override
	public final void store(
		final Binary                           bytes   ,
		final PersistenceRootReference.Default instance,
		final long                             objectId,
		final PersistenceStoreHandler          handler
	)
	{
		// (10.12.2019 TM)FIXME: priv#194
		bytes.storeRoots(this.typeId(), objectId, instance.entries(), handler);
	}

	@Override
	public final PersistenceRootReference.Default create(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		/* (10.12.2019 TM)TODO: PersistenceRoots constants instance oid association
		 * This method could collect all oids per identifer in the binary data and associate all
		 * linkable constants instances with their oid at the objectRegistry very easily and elegantly, here.
		 * Then there wouldn't be unnecessarily created instances that get discarded later on in update().
		 */
		// (10.12.2019 TM)FIXME: priv#194
		return this.instance;
	}

	@Override
	public final void update(
		final Binary                           bytes   ,
		final PersistenceRootReference.Default instance,
		final PersistenceObjectIdResolver      handler
	)
	{
		// (10.12.2019 TM)FIXME: priv#194
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
