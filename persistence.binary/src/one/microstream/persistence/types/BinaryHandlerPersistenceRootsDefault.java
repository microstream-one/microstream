package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.typing.KeyValue;


public final class BinaryHandlerPersistenceRootsDefault
extends AbstractBinaryHandlerCustom<PersistenceRoots.Default>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long OFFSET_OID_LIST = 0;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPersistenceRootsDefault New(
		final PersistenceRootResolverProvider rootResolverProvider,
		final PersistenceObjectRegistry       globalRegistry
	)
	{
		return new BinaryHandlerPersistenceRootsDefault(
			notNull(rootResolverProvider),
			notNull(globalRegistry)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceRootResolverProvider rootResolverProvider;

	/**
	 * The handler instance directly known the global registry might suprise at first and seem like a shortcut hack.
	 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
	 * it becomes clear that a direct access for registering resolved global instances at the global registry is
	 * indeed part of this handler's task
	 */
	final PersistenceObjectRegistry globalRegistry;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPersistenceRootsDefault(
		final PersistenceRootResolverProvider rootResolverProvider,
		final PersistenceObjectRegistry       globalRegistry
	)
	{
		super(
			PersistenceRoots.Default.class,
			CustomFields( // instances first to easy ref-only loading in storage
				Complex("instances",
					CustomField(Object.class, "instance")
				),
				Complex("identifiers",
					chars("identifier")
				)
			)
		);
		this.rootResolverProvider = rootResolverProvider;
		this.globalRegistry       = globalRegistry      ;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                   bytes   ,
		final PersistenceRoots.Default instance,
		final long                     objectId,
		final PersistenceStoreHandler  handler
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

	private static void fillObjectIds(final long[] oids, final Binary bytes)
	{
		final long offsetOidData = bytes.binaryListElementsAddress(OFFSET_OID_LIST);
		bytes.internalRead_longs(offsetOidData, oids);
	}

	private static void fillIdentifiers(final String[] identifiers, final Binary bytes)
	{
		final long offsetIdentifierList = bytes.getBinaryListTotalByteLength(OFFSET_OID_LIST);

		bytes.buildStrings(offsetIdentifierList, identifiers);
	}

	private void registerInstancesPerObjectId(
		final XGettingTable<String, PersistenceRootEntry> resolvedRootEntries,
		final XGettingTable<String, Long>                 rootIdMapping
	)
	{
		final PersistenceObjectRegistry registry = this.globalRegistry;

		// lock the whole registry for the complete registration process because it might be used by other threads.
		synchronized(registry)
		{
			for(final KeyValue<String, PersistenceRootEntry> rootEntry : resolvedRootEntries)
			{
				final Object rootInstance = rootEntry.value().instance();
				
				// instances can be null when either explicitly registered to be null in the refactoring or legacy enum
				if(rootInstance != null)
				{
					// must be the original identifier, not the potentially re-mapped identifier of the entry!
					final Long rootObjectId = rootIdMapping.get(rootEntry.key());
					
					// all live instances are registered for their OID.
					registry.registerConstant(rootObjectId.longValue(), rootInstance);
				}
			}
		}
	}

	@Override
	public final PersistenceRoots.Default create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return PersistenceRoots.Default.New(
			this.rootResolverProvider.provideRootResolver()
		);
	}

	@Override
	public final void update(
		final Binary                      bytes   ,
		final PersistenceRoots.Default    instance,
		final PersistenceObjectIdResolver handler
	)
	{
		// The once provided and then set root resolver is used right away in here.
		final PersistenceRootResolver rootResolver = instance.rootResolver;
		
		// The identifier -> objectId root id mapping is created (and validated) from the loaded data.
		final XGettingTable<String, Long> rootIdMapping = createRootMapping(bytes);

		// Root identifiers are resolved to root entries (with potentially mapped (= different) identifiers internally)
		final XGettingTable<String, PersistenceRootEntry> resolvedRootEntries = rootResolver.resolveRootEntries(
			rootIdMapping.keys()
		);
		
		// The entries are resolved to a mapping of current (= potentially mapped) identifiers to root instances.
		final XGettingTable<String, Object> resolvedRoots = rootResolver.resolveRootInstances(resolvedRootEntries);
		
		// The root instance's entries are updated (replaced) with the ones resolved in here.
		instance.loadingUpdateEntries(resolvedRoots);
		
		// The resolved instances need to be registered for their objectIds. Properly mapped to consider removed ones.
		this.registerInstancesPerObjectId(resolvedRootEntries, rootIdMapping);
	}
	
	private static XGettingTable<String, Long> createRootMapping(final Binary bytes)
	{
		/*
		 * A little detour for easier debuggability. But note that performance
		 * is not important here as roots only get loaded once per system start
		 * and are very few in numbers.
		 */
		final long[]   objectIds   = buildTempObjectIdArray(bytes);
		final String[] identifiers = buildTempIdentifiersArray(bytes);

		validateArrayLengths(objectIds, identifiers);
		fillObjectIds(objectIds, bytes);
		fillIdentifiers(identifiers, bytes);

		// To really validate consistency completely
		final EqHashEnum<Long> objectIdUniquenessChecker = EqHashEnum.New();
		
		final EqHashTable<String, Long> rootMapping = EqHashTable.New();
		for(int i = 0; i < objectIds.length; i++)
		{
			if(!objectIdUniquenessChecker.add(objectIds[i]))
			{
				// (02.09.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Persisted root entries have a duplicate root objectId for entry ("
					+ identifiers[i] + " -> " + objectIds[i] + ")"
				);
			}
			
			if(!rootMapping.add(identifiers[i], objectIds[i]))
			{
				// (02.09.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Persisted root entries have a duplicate root identifiers for entry ("
					+ identifiers[i] + " -> " + objectIds[i] + ")"
				);
			}
		}
		
		return rootMapping;
	}
	
	@Override
	public final void iterateInstanceReferences(
		final PersistenceRoots.Default instance,
		final PersistenceFunction      iterator
	)
	{
		// root identifiers are actually stored a an array of string values, not as string instances.
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
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
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
