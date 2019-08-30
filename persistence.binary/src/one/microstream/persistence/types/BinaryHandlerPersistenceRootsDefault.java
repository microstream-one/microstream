package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;


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

	private void registerInstancesPerObjectId(final long[] oids, final XGettingSequence<Object> instances)
	{
		final PersistenceObjectRegistry registry = this.globalRegistry;

		// lock the whole registry for the complete registration process because it is definitely used by other threads
		synchronized(registry)
		{
			int i = 0;
			for(final Object instance : instances)
			{
				// instances can be null when either explicitly registered to be null in the refactoring or legacy enum
				if(instance != null)
				{
					// all live instances are registered for their OID.
					registry.registerConstant(oids[i], instance);
				}
				i++;
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
		final Binary                   bytes   ,
		final PersistenceRoots.Default instance,
		final PersistenceObjectIdResolver   handler
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
				
		// the once provided and then set root resolver is used right away
		final PersistenceRootResolver rootResolver = instance.rootResolver;

		final XGettingTable<String, PersistenceRootEntry> resolvableRoots = rootResolver.resolveRootEntries(
			EqHashEnum.New(identifiers)
		);
		final XGettingTable<String, Object> resolvedRoots = rootResolver.resolveRootInstances(resolvableRoots);
			
		instance.loadingUpdateEntries(resolvedRoots);


		// (30.08.2019 TM)NOTE: fix for priv#138, but no longer required since nulls are no longer filtered out.
//		final long[] updatedObjectIds = updateObjectIds(objectIds, resolvableRoots.keys(), resolvedRoots.keys());
//		this.registerInstancesPerObjectId(updatedObjectIds, resolvedRoots.values());
		
		// (30.08.2019 TM)NOTE: due to changed to #resolveRootInstances via priv#23, this is now correct.
		this.registerInstancesPerObjectId(objectIds, resolvedRoots.values());
	}
	
	// (30.08.2019 TM)NOTE: fix for priv#138, but no longer required since nulls are no longer filtered out.
//	private static long[] updateObjectIds(
//		final long[]               objectIds,
//		final XGettingEnum<String> oldKeys  ,
//		final XGettingEnum<String> newKeys
//	)
//	{
//		if(oldKeys.size() == newKeys.size())
//		{
//			// array is up to date, return right away.
//			return objectIds;
//		}
//
//		final long[] newObjectIds = new long[newKeys.intSize()];
//
//		int o = 0, n = 0;
//		final Iterator<String> oldKeysIterator = oldKeys.iterator();
//		final Iterator<String> newKeysIterator = newKeys.iterator();
//
//		while(newKeysIterator.hasNext())
//		{
//			final String newKey = newKeysIterator.next();
//
//			while(!newKey.equals(oldKeysIterator.next()))
//			{
//				// skip oid index of removed key
//				o++;
//			}
//
//			// corresponding entries found
//			newObjectIds[n++] = objectIds[o++];
//		}
//
//		return newObjectIds;
//	}

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
