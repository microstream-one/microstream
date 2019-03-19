package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootEntry;
import one.microstream.persistence.types.PersistenceRootResolver;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerPersistenceRootsImplementation
extends AbstractBinaryHandlerCustom<PersistenceRoots.Implementation>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final long OFFSET_OID_LIST = 0;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPersistenceRootsImplementation New(
		final PersistenceRootResolver resolver      ,
		final PersistenceObjectRegistry         globalRegistry
	)
	{
		return new BinaryHandlerPersistenceRootsImplementation(
			notNull(resolver)      ,
			notNull(globalRegistry)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceRootResolver resolver;

	/**
	 * The handler instance directly known the global registry might suprise at first and seem like a shortcut hack.
	 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
	 * it becomes clear that a direct access for registering resolved global instances at the global registry is
	 * indeed part of this handler's task
	 */
	final PersistenceObjectRegistry globalRegistry;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	BinaryHandlerPersistenceRootsImplementation(
		final PersistenceRootResolver resolver      ,
		final PersistenceObjectRegistry         globalRegistry
	)
	{
		super(
			PersistenceRoots.Implementation.class,
			pseudoFields( // instances first to easy ref-only loading in storage
				complex("instances",
					pseudoField(Object.class, "instance")
				),
				complex("identifiers",
					chars("identifier")
				)
			)
		);
		this.resolver       = resolver      ;
		this.globalRegistry = globalRegistry;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          bytes   ,
		final PersistenceRoots.Implementation instance,
		final long                            objectId,
		final PersistenceStoreHandler         handler
	)
	{
		bytes.storeRoots(this.typeId(), objectId, instance.entries(), handler);
	}

	private static long[] buildTempObjectIdArray(final Binary bytes)
	{
		final long amountOids = bytes.getBinaryListElementCountUnvalidating(OFFSET_OID_LIST);
		return new long[X.checkArrayRange(amountOids)];
	}

	private static String[] buildTempIdentifiersArray(final Binary bytes)
	{
		final long offsetIdentifierList = bytes.getBinaryListTotalByteLength(OFFSET_OID_LIST);
		final long amountIdentifiers    = bytes.getBinaryListElementCountUnvalidating(offsetIdentifierList);

		return new String[X.checkArrayRange(amountIdentifiers)];
	}

	private static void validateArrayLengths(final long[] oids, final String[] identifiers)
	{
		if(oids.length != identifiers.length)
		{
			// just to be safe
			throw new RuntimeException(); // (21.10.2013 TM)EXCP: proper exception
		}
	}

	private void fillObjectIds(final long[] oids, final Binary bytes)
	{
		final long offsetOidData = bytes.binaryListElementsAddress(OFFSET_OID_LIST);
		bytes.read_longs(offsetOidData, oids);
	}

	private void fillIdentifiers(final String[] identifiers, final Binary bytes)
	{
		final long offsetIdentifierList = bytes.getBinaryListTotalByteLength(OFFSET_OID_LIST);

		bytes.buildStrings(offsetIdentifierList, identifiers);
	}

	private void registerInstancesPerObjectId(final long[] oids, final Object[] instances)
	{
		final PersistenceObjectRegistry registry = this.globalRegistry;

		// lock the whole registry for the complete registration process because it is definitely used by other threads
		synchronized(registry)
		{
			for(int i = 0; i < oids.length; i++)
			{
				// instances can be null when they are explicitly registered to be null in the refactoring
				if(instances[i] == null)
				{
					continue;
				}
				
				// all still live instances are registered for their OID.
				registry.registerConstant(oids[i], instances[i]);
			}
		}
	}

	@Override
	public final PersistenceRoots.Implementation create(final Binary bytes)
	{
		return PersistenceRoots.Implementation.createUninitialized();
	}

	@Override
	public final void update(
		final Binary                          bytes   ,
		final PersistenceRoots.Implementation instance,
		final PersistenceLoadHandler          handler
	)
	{
		/*
		 * Note that performance is not important here as roots only get loaded once per system start
		 * and are very few in numbers (hence the temp array copying detour).
		 * Also the temp arrays allow shorter lock times on the global registry.
		 */

		final long[]   objectIds   = buildTempObjectIdArray(bytes);
		final String[] identifiers = buildTempIdentifiersArray(bytes);

		validateArrayLengths(objectIds, identifiers);
		this.fillObjectIds(objectIds, bytes);
		this.fillIdentifiers(identifiers, bytes);

		final XGettingTable<String, PersistenceRootEntry> resolvedRoots = this.resolver.resolveRootInstances(
			EqHashEnum.New(identifiers)
		);
		final Object[] instances = instance.setResolvedRoots(resolvedRoots);
		this.registerInstancesPerObjectId(objectIds, instances);
	}

	@Override
	public final void iterateInstanceReferences(
		final PersistenceRoots.Implementation instance,
		final PersistenceFunction                 iterator
	)
	{
		// the identifier strings are not considered instances (that are worth iterating/knowing) but mere value types
		for(final Object object : instance.entries().values())
		{
			iterator.apply(object);
		}
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		// the nice thing about this layout is: the references can be accessed directly as if it was a simple list
		bytes.iterateListElementReferences(0, iterator);
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

}
