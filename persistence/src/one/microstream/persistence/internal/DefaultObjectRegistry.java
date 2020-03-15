package one.microstream.persistence.internal;

import static one.microstream.X.KeyValue;

import java.lang.ref.WeakReference;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashStatisticsBucketBased;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyObject;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import one.microstream.persistence.exceptions.PersistenceExceptionImproperObjectId;
import one.microstream.persistence.exceptions.PersistenceExceptionInvalidObjectRegistryCapacity;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.reference.Swizzling;
import one.microstream.typing.KeyValue;

public final class DefaultObjectRegistry implements PersistenceObjectRegistry
{
	/* Notes on byte size per entry (+/- COOPS):
	 * - An Entry instance occupies 48/80 bytes (12/16 bytes header, 4 references, 1 long, 1 int, 2 references).
	 * - With hash density 1.0, every entry also occupies 2 additional references (8/16 bytes) in the hash tables.
	 * 
	 * Conclusion:
	 * The major memory eater is the rather big JDK WeakReference implementation, but it sadly is the only way to
	 * get the essential weak referencing semantic. A weak referencing array would be incredibly more efficient,
	 * but, of course, the Java developers didn't think about that.
	 * The rest is already so optimized as to memory consumption and performance, that choosing a different
	 * hash density makes only a small difference.
	 * 
	 * Hash Density values (+/- COOPS):
	 * 0.75f: 60/104 bytes per entry, 110% performance.
	 * 1.00f: 56/ 96 bytes per entry, 100% performance.
	 * 2.00f: 52/ 88 bytes per entry,  80% performance.
	 */

	/* (27.11.2018 TM)TODO: ObjectRegistry housekeeping thread
	 * - tryCleanUp() method to random-sample-check for trimmable buckets arrays (empty buckets and/or orphans)
	 * - thread with weak back-reference to this registry to make it stop automatically.
	 * - "lastRegister" timestamp to not interrupt registering-heavy phases.
	 * - some special methods like ensureCapacity must set the timestamp, too, to avoid counterproductive behavior.
	 * - the usual config values for check intervals, sample size, etc.
	 * - start() and stop() method in the registry for explicit control.
	 * - a size increase ensures the thread is running, any clear/truncate terminates it.
	 */

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	public static final float defaultHashDensity()
	{
		return 1.0f;
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final boolean isValidHashDensity(final float desiredHashDensity)
	{
		return XHashing.isValidHashDensity(desiredHashDensity);
	}

	public static final float validateHashDensity(final float desiredHashDensity)
	{
		return XHashing.validateHashDensity(desiredHashDensity);
	}
	
	public static final boolean isValidCapacity(final long desiredCapacity)
	{
		return desiredCapacity > 0;
	}

	public static final long validateCapacity(final long desiredCapacity)
	{
		if(!isValidCapacity(desiredCapacity))
		{
			throw new PersistenceExceptionInvalidObjectRegistryCapacity(desiredCapacity);
		}
		
		return desiredCapacity;
	}
	
	static final int hash(final Object object)
	{
		return System.identityHashCode(object);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static constructors //
	////////////////////////
	
	public static DefaultObjectRegistry New()
	{
		return New(defaultHashDensity());
	}

	public static DefaultObjectRegistry New(final long minimumCapacity)
	{
		return New(defaultHashDensity(), minimumCapacity);
	}

	public static DefaultObjectRegistry New(final float hashDensity)
	{
		return New(hashDensity, 1);
	}

	/**
	 * Note on hashDensity: Reasonable values are within [0.75; 2.00].
	 * 
	 * @param hashDensity
	 * @param minimumCapacity
	 */
	public static DefaultObjectRegistry New(
		final float hashDensity    ,
		final long  minimumCapacity
	)
	{
		return new DefaultObjectRegistry()
			.internalSetConfiguration(
				validateHashDensity(hashDensity),
				validateCapacity(minimumCapacity)
			)
			.internalReset()
		;
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private Entry[] oidHashTable;
	private Entry[] refHashTable;
	private int     hashRange   ; // bit mask / modulo value used for hashing.
	private float   hashDensity ; // average number of buckets per hash table slot for well-distributed hash values.
	private long    capacity    ; // upper rebuild threshold.
	private long    minCapacity ; // minimum capacity
	private long    size        ;
	
	// integrated special constants registry
	private EqHashTable<Long, Object> constantsHotRegistry = EqHashTable.New();
	private Object[]                  constantsColdStorageObjects  ;
	private long[]                    constantsColdStorageObjectIds;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	DefaultObjectRegistry()
	{
		super();
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private int internalHashLength()
	{
		return this.hashRange + 1;
	}
	
	private void internalSetHashDensity(final float validHashDensity)
	{
		this.hashDensity = validHashDensity;
	}
	
	private void internalSetMinimumCapacity(final long validMinimumCapacity)
	{
		this.minCapacity = validMinimumCapacity;
	}
	
	final DefaultObjectRegistry internalSetConfiguration(
		final float hashDensity    ,
		final long  minimumCapacity
	)
	{
		this.internalSetHashDensity(hashDensity);
		this.internalSetMinimumCapacity(minimumCapacity);
		
		return this;
	}
	
	final DefaultObjectRegistry internalReset()
	{
		return this.internalReset(this.minCapacity);
	}
	
	final DefaultObjectRegistry internalReset(final long minimumCapacity)
	{
		this.size = 0;
		final int hashLength = this.calculateRequiredHashLength(minimumCapacity);
		this.setHashTables(
			this.createHashTable(hashLength),
			this.createHashTable(hashLength)
		);
				
		return this;
	}
	
	private int calculateRequiredHashLength(final long minimumCapacity)
	{
		return XHashing.padHashLength((long)(minimumCapacity / this.hashDensity));
	}
		
	private void setHashTables(final Entry[] oidHashTable, final Entry[] refHashTable)
	{
		this.oidHashTable = oidHashTable;
		this.refHashTable = refHashTable;
		this.hashRange    = oidHashTable.length - 1;
		this.internalUpdateCapacity();
	}
		
	private Entry[] createHashTable(final int hashLength)
	{
		return new Entry[hashLength];
	}
	
	private void internalUpdateCapacity()
	{
		this.capacity = this.internalHashLength() >= XMath.highestPowerOf2_int()
			? Long.MAX_VALUE
			: (long)(this.internalHashLength() * this.hashDensity)
		;
	}
		
	@Override
	public final synchronized DefaultObjectRegistry Clone()
	{
		return DefaultObjectRegistry.New(this.hashDensity, this.minCapacity);
	}

	@Override
	public final synchronized int hashRange()
	{
		return this.oidHashTable.length;
	}

	@Override
	public final synchronized float hashDensity()
	{
		return this.hashDensity;
	}
	
	@Override
	public final synchronized long minimumCapacity()
	{
		return this.minCapacity;
	}

	@Override
	public final synchronized long capacity()
	{
		return this.capacity;
	}

	@Override
	public final synchronized long size()
	{
		return this.size;
	}

	@Override
	public final synchronized boolean isEmpty()
	{
		return this.size == 0;
	}
	
	@Override
	public final synchronized boolean setHashDensity(final float hashDensity)
	{
		this.internalSetHashDensity(validateHashDensity(hashDensity));
		
		this.internalUpdateCapacity();
		return this.ensureCapacity(this.minCapacity);
	}
	
	@Override
	public final synchronized boolean setConfiguration(
		final float hashDensity    ,
		final long  minimumCapacity
	)
	{
		// both values are checked before modifying any state
		validateHashDensity(hashDensity);
		validateCapacity(minimumCapacity);
		
		this.internalSetHashDensity(hashDensity);
		this.internalSetMinimumCapacity(minimumCapacity);
		
		this.internalUpdateCapacity();
		return this.ensureCapacity(minimumCapacity);
	}
	
	@Override
	public final synchronized boolean setMinimumCapacity(final long minimumCapacity)
	{
		this.internalSetMinimumCapacity(validateCapacity(minimumCapacity));
		
		this.internalUpdateCapacity();
		return this.ensureCapacity(minimumCapacity);
	}
	
	@Override
	public final synchronized boolean ensureCapacity(final long desiredCapacity)
	{
		/*
		 * Cannot use capacityHigh here, as this method is called after changing capacity-defining values.
		 * Instead, the actual hash length is checked to determine if the tables really are too small.
		 */
		
		validateCapacity(desiredCapacity);
		final int requiredHashLength = this.calculateRequiredHashLength(desiredCapacity);
		if(requiredHashLength > this.internalHashLength())
		{
			this.internalRebuild(requiredHashLength);
			
			return true;
		}
		
		return false;
	}

	@Override
	public final synchronized boolean containsObjectId(final long objectId)
	{
		for(Entry e = this.oidHashTable[(int)objectId & this.hashRange]; e != null; e = e.oidNext)
		{
			if(e.objectId == objectId)
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public final synchronized long lookupObjectId(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}

		for(Entry e = this.refHashTable[hash(object) & this.hashRange]; e != null; e = e.refNext)
		{
			if(e.get() == object)
			{
				return e.objectId;
			}
		}
		
		return Swizzling.notFoundId();
	}

	@Override
	public final synchronized Object lookupObject(final long objectId)
	{
		for(Entry e = this.oidHashTable[(int)objectId & this.hashRange]; e != null; e = e.oidNext)
		{
			if(e.objectId == objectId)
			{
				return e.get();
			}
		}
		
		return null;
	}
	
	@Override
	public final synchronized boolean registerObject(final long objectId, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(!Swizzling.isProperId(objectId))
		{
			throw new PersistenceExceptionImproperObjectId();
		}

		return this.internalAdd(objectId, object);
	}

	@Override
	public final synchronized Object optionalRegisterObject(final long objectId, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(!Swizzling.isProperId(objectId))
		{
			throw new PersistenceExceptionImproperObjectId();
		}
		
		return this.internalAddGet(objectId, object);
	}
	
	@Override
	public final synchronized boolean registerConstant(final long objectId, final Object constant)
	{
		if(!this.registerObject(objectId, constant))
		{
			return false;
		}
		
		this.ensureConstantsHotRegistry().add(objectId, constant);
		
		return true;
	}

	@Override
	public final synchronized <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		iterateEntries(this.oidHashTable, acceptor);
		return acceptor;
	}
		
	private boolean internalAdd(final long objectId, final Object object)
	{
		if(this.internalAddCheck(objectId, object))
		{
			return false;
		}

		this.internalPutNewEntry(objectId, object);
		return true;
	}
		
	private void internalPutNewEntry(final long objectId, final Object object)
	{
		this.oidHashTable[(int)objectId & this.hashRange] =
		this.refHashTable[ hash(object) & this.hashRange] =
			new Entry(
				objectId,
				object,
				this.oidHashTable[(int)objectId & this.hashRange],
				this.refHashTable[ hash(object) & this.hashRange]
			)
		;
		
		if(++this.size > this.capacity)
		{
			this.internalIncreaseStorage();
		}
	}
	
	private boolean internalAddCheck(final long objectId, final Object object)
	{
		for(Entry e = this.oidHashTable[(int)objectId & this.hashRange]; e != null; e = e.oidNext)
		{
			if(e.objectId == objectId)
			{
				return this.internalHandleExisting(object, e);
			}
		}

		this.internalValidateObjectNotYetRegistered(objectId, object);
		return false;
	}
	
	private Object internalAddGetCheck(final long objectId, final Object object)
	{
		for(Entry e = this.oidHashTable[(int)objectId & this.hashRange]; e != null; e = e.oidNext)
		{
			if(e.objectId == objectId)
			{
				final Object registered;
				if((registered = e.get()) != null)
				{
					return registered;
				}

				// orphan entry removal is always right, even in case of an error.
				this.internalRemoveEntry(e);
				break;
			}
		}

		// either no hash chain yet or no (live) entry for that objectId. Validate and signal need for registration.
		this.internalValidateObjectNotYetRegistered(objectId, object);
		
		return null;
	}
	
	private boolean internalHandleExisting(final Object object, final Entry entry)
	{
		if(entry.get() == object)
		{
			return true;
		}
		
		if(entry.get() != null)
		{
			throw new PersistenceExceptionConsistencyObject(entry.objectId, entry.get(), object);
		}
		
		this.internalValidateObjectNotYetRegistered(entry.objectId, object);
		this.internalRemoveEntry(entry);
		
		return false;
	}
	
	private void internalRemoveEntry(final Entry entry)
	{
		removeFromOidTable(this.oidHashTable, (int)entry.objectId & this.hashRange, entry);
		removeFromRefTable(this.refHashTable,      entry.refHash  & this.hashRange, entry);
		this.size--;
	}
	
	private static void removeFromOidTable(final Entry[] table, final int index, final Entry entry)
	{
		for(Entry e = table[index], last = null; e != null; e = (last = e).oidNext)
		{
			if(e == entry)
			{
				if(last == null)
				{
					table[index] = e.oidNext;
				}
				else
				{
					last.oidNext = e.oidNext;
				}
			}
		}
	}
	
	private static void removeFromRefTable(final Entry[] table, final int index, final Entry entry)
	{
		for(Entry e = table[index], last = null; e != null; e = (last = e).refNext)
		{
			if(e == entry)
			{
				if(last == null)
				{
					table[index] = e.refNext;
				}
				else
				{
					last.refNext = e.refNext;
				}
			}
		}
	}
		
	private void internalValidateObjectNotYetRegistered(final long objectId, final Object object)
	{
		final int refHash = hash(object);
		for(Entry e = this.refHashTable[refHash & this.hashRange]; e != null; e = e.refNext)
		{
			// intentionally no check of refHash first as the hash table is assumed to be rather flat.
			if(e.get() == object)
			{
				throw new PersistenceExceptionConsistencyObjectId(object, e.objectId, objectId);
			}
		}
	}

	private Object internalAddGet(final long objectId, final Object object)
	{
		final Object alreadyRegistered;
		if((alreadyRegistered = this.internalAddGetCheck(objectId, object)) != null)
		{
			return alreadyRegistered;
		}

		this.internalPutNewEntry(objectId, object);
		return object;
	}
	
	// rebuilding and consolidation //
	
	@Override
	public final synchronized boolean consolidate()
	{
		// both tables always have the same length
		final Entry[] oidHashTable = this.oidHashTable;
		final Entry[] refHashTable = this.refHashTable;
		
		int orphanCount = 0;
		for(int h = 0; h < oidHashTable.length; h++)
		{
			// the primary branch (per objectIds) is used to determine the orphan count.
			orphanCount += consolidateOidHashChain(oidHashTable, h);
			
			// the secondard branch is just updated to avoid counting the same orphan entry twice.
			consolidateRefHashChain(refHashTable, h);
		}
		
		this.size -= orphanCount;
		
		return this.checkForDecrease();
	}
	
	private static int consolidateOidHashChain(final Entry[] oidHashTable, final int h)
	{
		int orphanCount = 0;
		for(Entry e = oidHashTable[h], lastProper = null; e != null; e = e.oidNext)
		{
			if(e.get() != null)
			{
				// everything stays as it is.
				lastProper = e;
				continue;
			}
			
			// orphaned entry is removed. The first entry in the chain is a special case to be handled.
			if(lastProper == null)
			{
				oidHashTable[h] = e.oidNext;
			}
			else
			{
				lastProper.oidNext = e.oidNext;
			}
			orphanCount++;
		}
		
		return orphanCount;
	}
	
	private static void consolidateRefHashChain(final Entry[] refHashTable, final int h)
	{
		for(Entry e = refHashTable[h], lastProper = null; e != null; e = e.refNext)
		{
			if(e.get() != null)
			{
				// everything stays as it is.
				lastProper = e;
				continue;
			}
			
			// orphaned entry is removed. The first entry in the chain is a special case to be handled.
			if(lastProper == null)
			{
				refHashTable[h] = e.refNext;
			}
			else
			{
				lastProper.refNext = e.refNext;
			}
		}
	}
		
	private boolean checkForDecrease()
	{
		final int requiredHashLength = this.calculateRequiredHashLength(this.size);
		if(requiredHashLength != this.internalHashLength())
		{
			this.internalRebuild(requiredHashLength);
			
			return true;
		}
		
		return false;
	}
	
	private void internalIncreaseStorage()
	{
		// capacityHighBound checks prevent unnecessary / dangerous calls of this method
		this.internalRebuild(this.oidHashTable.length << 1);
	}
	
	private void internalRebuild(final int hashLength)
	{
		final Entry[] newOidHashTable = this.createHashTable(hashLength);
		final Entry[] newRefHashTable = this.createHashTable(hashLength);

		// orphaned entries are discarded and their total count is returned to be subtracted here.
		this.size -= rebuildTables(this.oidHashTable, newOidHashTable, newRefHashTable);
		
		// the new hash tables are set as the instance's storage structure.
		this.setHashTables(newOidHashTable, newRefHashTable);
		
		/*
		 * Since rebuilding discards orphaned entries and reduces the size, it could be possible that
		 * a rebuild to increase the storage determines that it could actually shrink.
		 * The doubled performance cost in such cases should be well worth the automatic memory saving.
		 */
		this.checkForDecrease();
		
		// at some point, constant registration is completed, so an efficient storage form is preferable.
		this.internalEnsureConstantsColdStorage();
	}

	private static long rebuildTables(
		final Entry[] oldOidHashTable,
		final Entry[] newOidHashTable,
		final Entry[] newRefHashTable
	)
	{
		final int hashRange = newOidHashTable.length - 1;
		
		long orphanCount = 0;
		for(int i = 0; i < oldOidHashTable.length; i++)
		{
			if(oldOidHashTable[i] == null)
			{
				continue;
			}

			orphanCount += rebuildEntryChain(oldOidHashTable[i], hashRange, newOidHashTable, newRefHashTable);
		}
		
		return orphanCount;
	}

	private static int rebuildEntryChain(
		final Entry   firstOidHashEntry,
		final int     hashRange        ,
		final Entry[] newOidHashTable  ,
		final Entry[] newRefHashTable
	)
	{
		int orphanCount = 0;
		Entry e = firstOidHashEntry, next;
		do
		{
			next = e.oidNext;
			if(e.get() != null)
			{
				e.oidNext = newOidHashTable[(int)e.objectId & hashRange];
				e.refNext = newRefHashTable[     e.refHash  & hashRange];
				newOidHashTable[(int)e.objectId & hashRange] = e;
				newRefHashTable[     e.refHash  & hashRange] = e;
			}
			else
			{
				orphanCount++;
			}
		}
		while((e = next) != null);
		
		return orphanCount;
	}

	private static void iterateEntries(final Entry[] oidHashTable, final PersistenceAcceptor acceptor)
	{
		for(int s = 0; s < oidHashTable.length; s++)
		{
			for(Entry e = oidHashTable[s]; e != null; e = e.oidNext)
			{
				acceptor.accept(e.objectId, e.get());
			}
		}
	}
	
	// clearing //
	
	@Override
	public final synchronized void clear()
	{
		this.internalEnsureConstantsColdStorage();
		this.internalClear();
		this.internalReregisterConstants();
	}
	
	@Override
	public final synchronized void clearAll()
	{
		this.internalClear();
	}
	
	private void internalClear()
	{
		final Entry[] oidBuckets = this.oidHashTable;
		final Entry[] refBuckets = this.refHashTable;
		
		for(int i = 0; i < oidBuckets.length; i++)
		{
			oidBuckets[i] = refBuckets[i] = null;
		}
		
		this.size = 0;
	}

	@Override
	public final synchronized void truncate()
	{
		// reinitialize storage strucuture with at least enough capacity for the incoming constants.
		this.internalEnsureConstantsColdStorage();
		
		this.internalReset(Math.max(this.constantsColdStorageObjects.length, this.minCapacity));
		
		this.internalReregisterConstants();
	}
	
	@Override
	public final synchronized void truncateAll()
	{
		// hash table reset, no constants reregistering.
		this.internalReset();
	}
	
	// Constants handling //
	
	private void internalReregisterConstants()
	{
		final Object[] constantsObjects = this.constantsColdStorageObjects;
		final long[] constantsObjectIds = this.constantsColdStorageObjectIds;
		
		for(int i = 0; i < constantsObjects.length; i++)
		{
			// NOT registerConstant() at this point!
			this.registerObject(constantsObjectIds[i], constantsObjects[i]);
		}
	}
	
	private EqHashTable<Long, Object> ensureConstantsHotRegistry()
	{
		if(this.constantsHotRegistry == null)
		{
			this.internalBuildConstantsHotRegistry();
		}
		
		return this.constantsHotRegistry;
	}
	
	private void internalBuildConstantsHotRegistry()
	{
		final EqHashTable<Long, Object> constantsHotRegistry = EqHashTable.New();
		
		final Object[] constantsObjects   = this.constantsColdStorageObjects  ;
		final long[]   constantsObjectIds = this.constantsColdStorageObjectIds;
				
		final int constantsLength = constantsObjects.length;
		for(int i = 0; i < constantsLength; i++)
		{
			constantsHotRegistry.add(constantsObjectIds[i], constantsObjects[i]);
		}
		
		this.constantsHotRegistry          = constantsHotRegistry;
		this.constantsColdStorageObjects   = null;
		this.constantsColdStorageObjectIds = null;
	}
	
	private void internalEnsureConstantsColdStorage()
	{
		if(this.constantsColdStorageObjects != null)
		{
			return;
		}
		
		this.internalBuildConstantsColdStorage();
	}
	
	private void internalBuildConstantsColdStorage()
	{
		
		final EqHashTable<Long, Object> constantsHotRegistry = this.constantsHotRegistry;
		
		final int      constantCount      = X.checkArrayRange(constantsHotRegistry.size());
		final Object[] constantsObjects   = new Object[constantCount];
		final long[]   constantsObjectIds = new long[constantCount];
		
		int i = 0;
		for(final KeyValue<Long, Object> e : constantsHotRegistry)
		{
			constantsObjects[i] = e.value();
			constantsObjectIds[i] = e.key();
			i++;
		}
		
		this.constantsHotRegistry          = null;
		this.constantsColdStorageObjects   = constantsObjects;
		this.constantsColdStorageObjectIds = constantsObjectIds;
	}
	
	// HashStatistics //
	
	@Override
	public final synchronized XGettingTable<String, HashStatisticsBucketBased> createHashStatistics()
	{
		return EqHashTable.New(
			KeyValue("PerObjectIds", this.internalCreateHashStatisticsOids()),
			KeyValue("PerObjects", this.internalCreateHashStatisticsRefs())
		);
	}
	
	private HashStatisticsBucketBased internalCreateHashStatisticsOids()
	{
		final EqHashTable<Long, Long> distributionTable = EqHashTable.New();
		
		final Entry[] oidHashTable = this.oidHashTable;
		for(int h = 0; h < oidHashTable.length; h++)
		{
			final Long bucketLength = countOidChainLength(oidHashTable[h]);
			registerDistribution(distributionTable, bucketLength);
		}
		complete(distributionTable);
		
		return HashStatisticsBucketBased.New(
			oidHashTable.length            ,
			this.size                      ,
			this.hashDensity               ,
			distributionTable.keys().last(),
			distributionTable
		);
	}

	private HashStatisticsBucketBased internalCreateHashStatisticsRefs()
	{
		final EqHashTable<Long, Long> distributionTable = EqHashTable.New();

		final Entry[] refHashTable = this.refHashTable;
		for(int h = 0; h < refHashTable.length; h++)
		{
			final Long bucketLength = countRefChainLength(refHashTable[h]);
			registerDistribution(distributionTable, bucketLength);
		}
		complete(distributionTable);
		
		return HashStatisticsBucketBased.New(
			refHashTable.length            ,
			this.size                      ,
			this.hashDensity               ,
			distributionTable.keys().last(),
			distributionTable
		);
	}
	
	private static Long countOidChainLength(final Entry firstEntry)
	{
		long count = 0;
		for(Entry e = firstEntry; e != null; e = e.oidNext)
		{
			if(e.get() != null)
			{
				count++;
			}
		}
		
		return count;
	}
	
	private static Long countRefChainLength(final Entry firstEntry)
	{
		long count = 0;
		for(Entry e = firstEntry; e != null; e = e.refNext)
		{
			if(e.get() != null)
			{
				count++;
			}
		}
		
		return count;
	}
	
	private static void registerDistribution(
		final EqHashTable<Long, Long> distributionTable,
		final Long                    bucketLength
	)
	{
		// rather inefficient. Could be done much more efficient if required.
		final Long count = distributionTable.get(bucketLength);
		if(count == null)
		{
			distributionTable.put(bucketLength, 1L);
		}
		else
		{
			distributionTable.put(bucketLength, count + 1L);
		}
	}
	
	private static void complete(final EqHashTable<Long, Long> distributionTable)
	{
		distributionTable.keys().sort(XSort::compare);
		final Long highest = distributionTable.last().key();
		final Long zero = 0L;
		
		for(long l = 0; l < highest; l++)
		{
			distributionTable.add(l, zero);
		}
		
		distributionTable.keys().sort(XSort::compare);
	}



	///////////////////////////////////////////////////////////////////////////
	// member types //
	/////////////////

	static final class Entry extends WeakReference<Object>
	{
		final long objectId;
		      int  refHash ;
		      Entry oidNext, refNext;
		
		Entry(final long objectId, final Object referent, final Entry oidNext, final Entry refnext)
		{
			super(referent);
			this.objectId = objectId;
			this.refHash  = hash(referent);
			this.oidNext  = oidNext;
			this.refNext  = refnext;
		}
		
	}
	
	
	
	public static final void printEntryInstanceSizeInfo()
	{
		// -XX:-UseCompressedOops -XX:+PrintGC
		XDebug.printInstanceSizeInfo(Entry.class);
	}

}
