package net.jadoth.persistence.binary.internal;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.persistence.types.PersistenceRootEntry;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerPersistenceRootsImplementation
extends AbstractBinaryHandlerNativeCustom<PersistenceRoots.Implementation>
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
		final long                            oid     ,
		final PersistenceStoreHandler                  handler
	)
	{
		// performance is not important here as roots only get stored once per system start and are very few in numbers
		final Object[] instances   = instance.entries().values().toArray();
		final String[] identifiers = instance.entries().keys().toArray(String.class);

		// calculate all the lengths
		final long instancesTotalBinLength    = BinaryPersistence.calculateReferenceListTotalBinaryLength(instances.length);
		final long identifiersConentBinLength = BinaryPersistence.calculateStringListContentBinaryLength(identifiers);
		final long totalContentLength         = instancesTotalBinLength
			+ BinaryPersistence.calculateListTotalBinaryLength(identifiersConentBinLength)
		;

		// store header for writing and reserving total length before writing content
		final long contentAddress = bytes.storeEntityHeader(totalContentLength, this.typeId(), oid);

		// store instances first to allow efficient references-only caching
		BinaryPersistence.storeArrayContentAsList(contentAddress, handler, instances, 0, instances.length);

		// store identifiers as list of inlined [char]s
		BinaryPersistence.storeStringsAsList(
			contentAddress + instancesTotalBinLength,
			identifiersConentBinLength,
			identifiers
		);
	}

	private static long[] buildTempObjectIdArray(final Binary bytes)
	{
		final long amountOids = BinaryPersistence.binaryArrayElementCount(bytes, OFFSET_OID_LIST);
		return new long[X.checkArrayRange(amountOids)];
	}

	private static String[] buildTempIdentifiersArray(final Binary bytes)
	{
		final long offsetIdentifierList = BinaryPersistence.binaryArrayByteLength(bytes, OFFSET_OID_LIST);
		final long amountIdentifiers    = BinaryPersistence.binaryArrayElementCount(bytes, offsetIdentifierList);

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
		final long offsetOidData = BinaryPersistence.binaryArrayElementDataAddress(bytes, OFFSET_OID_LIST);
		XMemory.copyRangeToArray(offsetOidData, oids);
	}

	private void fillIdentifiers(final String[] identifiers, final Binary bytes)
	{
		final long offsetIdentifierList = BinaryPersistence.binaryArrayByteLength(bytes, OFFSET_OID_LIST);

		BinaryPersistence.buildStrings(bytes, offsetIdentifierList, identifiers);
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
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		// the nice thing about this layout is: the references can be accessed directly as if it was a simple list
		BinaryPersistence.iterateListElementReferences(bytes, 0, iterator);
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
