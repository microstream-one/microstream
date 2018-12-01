package net.jadoth.persistence.internal;

import static net.jadoth.X.KeyValue;
import static net.jadoth.X.notNull;

import java.lang.ref.WeakReference;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hashing.HashStatisticsBucketBased;
import net.jadoth.hashing.Hashing;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObject;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceAcceptor;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.typing.KeyValue;


public final class DefaultObjectRegistry implements PersistenceObjectRegistry
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
	private static final Item[] EMPTY_REF_BUCKET = new Item[0];
	
	/* (27.11.2018 TM)TODO: ObjectRegistry housekeeping thread
	 * - optimize() method to trim bucket arrays and random-sample-check for orphans.
	 * - thread with weak back-reference to this registry to make it stop automatically.
	 * - "lastRegister" timestamp to not interrupt registering-heavy phases.
	 * - the usual config values for check intervals etc.
	 * - start() and stop() method in the registry for explicit control.
	 * - a size increase ensures the thread is running, a size of 0 terminates it.
	 */
	
	/* Notes:
	 * - all methods prefixed with "synch" are only called from inside a synchronized or another "synch" method.
	 * - the quad-bucket array storage is memory-inefficient for low density, but very efficient for high density.
	 * - sadly, WeakReferences occupy a lot of memory and there is no alternative to them for weakly referencing.
	 */
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
		
	public static final int defaultHashDensity()
	{
		/*
		 * Below that, the memory overhead for the quad-bucket-arrays does not pay off.
		 * Tests also showed best performance.
		 */
		return 8;
	}
		
	
	public static DefaultObjectRegistry New()
	{
		return New(defaultHashDensity());
	}
			
	public static DefaultObjectRegistry New(final int desiredHashDensity)
	{
		// there is no point in supporting a desired initial capacity when there's a capacity low bound for the size.
		
		return new DefaultObjectRegistry()
			.internalSetHashDensity(desiredHashDensity)
			.internalReset()
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private Item[][] oidHashedItemTable;
	private Item[][] refHashedItemTable;
	
	/**
	 * A measurement of how "dense" entries are packed in the hashing structure.<br>
	 * Higher density means less memory consumption but also lower performance.<br>
	 * See {@link #minimumHashDensity()}.
	 */
	private int hashDensity;
	
	/**
	 * The current length of the hash table master arrays. All four have always the same length.
	 */
	private int hashLength;
	
	/**
	 * Due to hashing arithmetic, a hash range is always in the form of 2^n - 1, or in shift-writing: (1<<n) - 1.
	 */
	private int hashRange;
		
	/**
	 * The amount of containable entries before a rebuild to increase the storage is required.
	 */
	private long capacityHigh;
	
	/**
	 * The amount of containable entries before a rebuild to decrease the storage is required.
	 */
	private long capacityLow;
	
	/**
	 * The total entry count (including existnig orphan entries)
	 */
	private long size;
	
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
	
	final DefaultObjectRegistry internalReset()
	{
		// starting low conserves memory if inactive and is even faster, despite the additional rebuild.
		return this.internalReset(1);
	}
	
	private static Item[][] createRefBucketTable(final int hashLength)
	{
		/*
		 * Weirdly enough, initializing all buckets as empty and then enlarge it is considerably faster
		 * than initialzing the bucket with the required size right away. No idea why.
		 */
		final Item[][] newTable = new Item[hashLength][];
		for(int i = 0; i < newTable.length; i++)
		{
			newTable[i] = EMPTY_REF_BUCKET;
		}
		
		return newTable;
	}
	
	final DefaultObjectRegistry internalReset(final int hashLength)
	{
		this.hashLength = hashLength;
		this.hashRange  = hashLength - 1;
		this.internalUpdateCapacities();
		this.size = 0;
		
		this.oidHashedItemTable = createRefBucketTable(hashLength);
		this.refHashedItemTable = createRefBucketTable(hashLength);
				
		return this;
	}
	
	final DefaultObjectRegistry internalSetHashDensity(final int hashDensity)
	{
		this.hashDensity = hashDensity;
		
		return this;
	}
		
	private void internalUpdateCapacities()
	{
		this.capacityHigh = this.hashLength * this.hashDensity;
		this.capacityLow  = this.hashLength == 1
			? 0
			: this.hashLength / 2 * this.hashDensity
		;
	}
	
	private int hash(final long objectId)
	{
		/* (27.11.2018 TM)XXX: test and comment hashing performance
		 * - hash(){ (int)objectId & this.hashRange
		 * - hash(){ (int)(objectId & this.hashRange)
		 * - hash(){ (int)(objectId & this.hashRangeLong)
		 * - inlined (int)objectId & this.hashRange
		 * - inlined (int)(objectId & this.hashRange)
		 * - inlined (int)(objectId & this.hashRangeLong)
		 * 
		 * Also cover rehashByObjectId()
		 */
		
		// an sign bit accidentally caused by the cast is irrelevant since the hash range will always suppress it.
		return (int)objectId & this.hashRange;
	}
	
	private int hash(final Object object)
	{
		/* (27.11.2018 TM)XXX: test and comment hashing performance
		 * - hash(){ System.identityHashCode(object) & this.hashRange
		 * - inlined System.identityHashCode(object) & this.hashRange
		 */
		
		// an sign bit accidentally caused by the cast is irrelevant since the hash range will always suppress it.
		return System.identityHashCode(object) & this.hashRange;
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
	public final synchronized int hashRange()
	{
		return this.hashRange;
	}
	
	@Override
	public final synchronized float hashDensity()
	{
		return this.hashDensity;
	}
		
	@Override
	public final synchronized DefaultObjectRegistry setHashDensity(final float hashDensity)
	{
		this.internalSetHashDensity((int)hashDensity);
		this.internalUpdateCapacities();
		this.synchCheckForRebuild();
		
		return this;
	}
	
	@Override
	public final synchronized long capacity()
	{
		return this.capacityHigh;
	}
	
	
	
	@Override
	public final synchronized long lookupObjectId(final Object object)
	{
		/* Potential Optimization:
		 * Only lock while the oidKeys bucket is queries.
		 * Could be reliably thread-local after that.
		 * (not exactely sure about the "reliably". Would have to be researched/tested)
		 */

		notNull(object);
		
		// The bucket can never be null. The first null terminates the bucket's entries.
		final Item[] refHashedRefKeys = this.refHashedItemTable[this.hash(object)];
		for(int i = 0; i < refHashedRefKeys.length && refHashedRefKeys[i] != null; i++)
		{
			// can be null for orphan entries
			if(refHashedRefKeys[i].get() == object)
			{
				// lookup and return the associated objectId (always located at the same index in its bucket)
				return refHashedRefKeys[i].objectId;
			}
		}
		
		// since null can never be contained, returning the null-id signals a miss.
		return Persistence.nullId();
	}
	
	@Override
	public final synchronized Object lookupObject(final long objectId)
	{
		/* Potential Optimization:
		 * Only lock while the oidKeys bucket is queries.
		 * Could be reliably thread-local after that.
		 * (not exactely sure about the "reliably". Would have to be researched/tested)
		 */
		
		// The bucket can never be null. The first 0 terminates the bucket's entries.
		final Item[] oidHashedItems = this.oidHashedItemTable[this.hash(objectId)];
		for(int i = 0; i < oidHashedItems.length && oidHashedItems[i] != null; i++)
		{
			if(oidHashedItems[i].objectId == objectId)
			{
				return oidHashedItems[i].get();
			}
		}
		
		// since null can never be contained, returning the null signals a miss.
		return null;
	}
	
	@Override
	public final synchronized boolean containsObjectId(final long objectId)
	{
		/* Potential Optimization:
		 * Only lock while the oidKeys bucket is queries.
		 * Could be reliably thread-local after that.
		 * (not exactely sure about the "reliably". Would have to be researched/tested)
		 */

		// The bucket can never be null. The first 0 terminates the bucket's entries.
		final Item[] oidHashedItems = this.oidHashedItemTable[this.hash(objectId)];
		for(int i = 0; i < oidHashedItems.length && oidHashedItems[i] != null; i++)
		{
			if(oidHashedItems[i].objectId == objectId)
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	
	@Override
	public final synchronized <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		// iterating everything has so many bucket accesses that a total lock is the only viable thing to do.
		
		final Item[][] oidHashedItemTable = this.oidHashedItemTable;
		
		for(int h = 0; h < oidHashedItemTable.length; h++)
		{
			// The buckets can never be null. The first 0/null terminates the bucket's entries.
			final Item[] oidHashedItems = oidHashedItemTable[h];
			
			for(int i = 0; i < oidHashedItems.length && oidHashedItems[i] != null; i++)
			{
				// orphan entries are passed intentionally to give this method a usage as an orphan analyzing tool.
				acceptor.accept(oidHashedItems[i].objectId, oidHashedItems[i].get());
			}
		}
		
		return acceptor;
	}
		
	@Override
	public final synchronized boolean registerObject(final long objectId, final Object object)
	{
//		XDebug.println("(Size " + this.size + ") Registering " + objectId + " <-> " + XChars.systemString(object));

		// case 1: the same object is already contained (indicated by null), so "no change" is reported.
		if(this.synchRegisterObject(objectId, object, false) == null)
		{
			return false;
		}
		
		// case 2: the object has been newly registered. Better check for a required rebuild.
		this.synchCheckForRebuild();
		
		// "change" is reported.
		return true;
	}
	
	@Override
	public final synchronized Object optionalRegisterObject(final long objectId, final Object object)
	{
		final Object registered;
		
		// case 1: the same object is already contained (indicated by null), so it is returned.
		if((registered = this.synchRegisterObject(objectId, object, true)) == null)
		{
			return object;
		}
		
		// case 2: the object has been newly registered. Better check for a required rebuild.
		if(registered == object)
		{
			// check for global rebuild after entries have changed (more or even fewer because of removed orphans)
			this.synchCheckForRebuild();
		}
		// else case 3: another object is already registered for that objectId.
		
		// return case 2/3 reference.
		return registered;
	}
	
	@Override
	public final synchronized boolean registerConstant(final long objectId, final Object constant)
	{
		if(!this.registerObject(objectId, constant))
		{
			return false;
		}
		
		this.synchEnsureConstantsHotRegistry().add(objectId, constant);
		
		return true;
	}
	
	private EqHashTable<Long, Object> synchEnsureConstantsHotRegistry()
	{
		if(this.constantsHotRegistry == null)
		{
			this.synchBuildConstantsHotRegistry();
		}
		
		return this.constantsHotRegistry;
	}
	
	private void synchBuildConstantsHotRegistry()
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
	
	private void synchEnsureConstantsColdStorage()
	{
		if(this.constantsColdStorageObjects != null)
		{
			return;
		}
		
		this.synchBuildConstantsColdStorage();
	}
	
	private void synchBuildConstantsColdStorage()
	{
		final EqHashTable<Long, Object> constantsHotRegistry = this.constantsHotRegistry;
		
		final int      constantCount      = X.checkArrayRange(constantsHotRegistry.size());
		final Object[] constantsObjects   = new Object[constantCount];
		final long[]   constantsObjectIds = new long[constantCount];
		
		final int i = 0;
		for(final KeyValue<Long, Object> e : constantsHotRegistry)
		{
			constantsObjects[i] = e.value();
			constantsObjectIds[i] = e.key();
		}

		this.constantsHotRegistry          = null;
		this.constantsColdStorageObjects   = constantsObjects;
		this.constantsColdStorageObjectIds = constantsObjectIds;
	}
	
	@Override
	public final synchronized void cleanUp()
	{
		// first check if the storage can be shrinked
		this.synchCheckForRebuild();
		
		// (30.11.2018 TM)XXX: not tested, yet.
		// then consolidate the buckets
		this.synchConsolidateBuckets();
	}
	
	private void synchConsolidateBuckets()
	{
		final Item[][] oidHashedItems = this.oidHashedItemTable;
		final int      hashLength     = this.hashLength;
		
		// this is also the only opportunity to recalculate the actual size without orphans.
		long size = 0;
		
		// both new branches are populated from the per-oid-branch
		for(int h = 0; h < hashLength; h++)
		{
			size += consolidateBuckets(oidHashedItems, h);
		}
		
		this.size = size;
	}
	
	private static int consolidateBuckets(
		final Item[][] refValsTable,
		final int      h
	)
	{
		final Item[] refVals = refValsTable[h];
		
		int requiredSize = 0;
		for(int i = 0; i < refVals.length && refVals[i] != null; i++)
		{
			if(refVals[i].get() != null)
			{
				requiredSize++;
			}
		}
		
		// the buckets are already completely filled. Nothing to do, here.
		if(requiredSize == refVals.length)
		{
			return requiredSize;
		}
		
		if(requiredSize == 0)
		{
			// no (linear) memory consumption but still viable for non-null-assuming logic.
			refValsTable[h] = EMPTY_REF_BUCKET;
			return 0;
		}
		
		final Item[] newRefVals = new Item[requiredSize];
		
		int t = 0;
		for(int i = 0; i < refVals.length && refVals[i] != null; i++)
		{
			if(refVals[i].get() != null)
			{
				newRefVals[t] = refVals[i];
				t++;
			}
		}
		
		/*
		 * It can happen that some entries became orphaned in the meantime and the new bucket remains unfilled.
		 * This is not perfectly efficient, but otherwise no problem.
		 * A system of weak referencing, where instances can "magically" disappear at any moment, has such implications.
		 */
		
		refValsTable[h] = newRefVals;
		
		return t;
	}
	
	@Override
	public final synchronized void clear()
	{
		// reinitialize storage strucuture with a suitable size for the incoming constants.
		this.synchEnsureConstantsColdStorage();
		
		final Object[] constantsObjects   = this.constantsColdStorageObjects  ;
		final long[]   constantsObjectIds = this.constantsColdStorageObjectIds;
		final int      constantsLength    = constantsObjects.length;
		final int      requiredHashLength = Hashing.calculateHashLength(constantsLength, this.hashDensity);
		
		this.internalReset(requiredHashLength);
		
		for(int i = 0; i < constantsLength; i++)
		{
			// NOT registerConstant() at this point!
			this.registerObject(constantsObjectIds[i], constantsObjects[i]);
		}
	}
	
	@Override
	public final synchronized void truncate()
	{
		// there is no point in keeping the old hash table arrays when there's a capacity low bound for the size.
		this.internalReset();
	}

	
	
	/* Performance notes:
	 * Tests from 100K to 5M entries showed that removing the optional flag does NOT improve performance.
	 * In some cases, it even got slightly worse.
	 * Using a preincrementing index in the loop (initialle -1 with ++i) showed no impact on performance.
	 */
	private Object synchRegisterObject(final long objectId, final Object object, final boolean optional)
	{
		// The buckets can never be null. The first 0/null terminates the bucket's entries.
		final Item[] oidHashedItems = this.oidHashedItemTable[this.hash(objectId)];

		int i = 0;
		while(i < oidHashedItems.length && oidHashedItems[i] != null)
		{
			if(oidHashedItems[i].objectId == objectId)
			{
				return this.synchHandleOidKeyBucketsMatch(i, objectId, object, optional);
			}
			i++;
		}
		
		// add in oid buckets, either new ones or existing ones or enlarged ones.
		
		// using item.oidHashIndex is faster than a local variable or multiple hashing.
		final Item item = new Item(objectId, object);
		
		this.synchAddInRefKeysBuckets(objectId, item);
		if(i < oidHashedItems.length)
		{
			// case 1: current buckets have a free slot.
			oidHashedItems[i] = item;
		}
		else
		{
			// case 2: existing buckets must be enlarged
			(this.oidHashedItemTable[this.hash(objectId)] =
				enlargeBucket(oidHashedItems, this.hashDensity))[i] = item
			;
		}
		
		this.size++;
		
		return object;
	}
					
	private Object synchHandleOidKeyBucketsMatch(
		final int     i       ,
		final long    objectId,
		final Object  object  ,
		final boolean optional
	)
	{
		final Item[] refVals = this.oidHashedItemTable[this.hash(objectId)];
		final Object alreadyRegistered;

		// case 1: object is already consistently registered, abort.
		if((alreadyRegistered = refVals[i].get()) == object)
		{
			return null;
		}

		// case 2: matching entry, but orphaned. Can and MUST be reused for a new Item.
		if(alreadyRegistered == null)
		{
			this.synchReplaceOrphanEntry(refVals[i], objectId, refVals[i] = new Item(objectId, object));
			return object;
		}
		
		// case 3a: another object is already registered for that objectId and shall be queried.
		if(optional)
		{
			return alreadyRegistered;
		}
		
		// case 3b: another object is inconsistently registered for that objectId, error.
		throw new PersistenceExceptionConsistencyObject(objectId, alreadyRegistered, object);
	}
						
	private void synchReplaceOrphanEntry(final Item orphanItem, final long objectId, final Item newItem)
	{
		final Item[] refHashedItems = this.refHashedItemTable[orphanItem.hash & this.hashRange];
		for(int i = 0; i < refHashedItems.length; i++)
		{
			if(refHashedItems[i] != orphanItem)
			{
				continue;
			}

			if(refHashedItems[i].objectId != objectId)
			{
				// (29.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Orphan entry object id inconsistency: " + refHashedItems[i] + " != " + objectId);
			}
			
			this.removeOrphanEntry(refHashedItems, i);
			this.synchAddInRefKeysBuckets(objectId, newItem);
			return;
		}

		// (29.11.2018 TM)EXCP: proper exception
		throw new RuntimeException("Orphan entry not found for object id " + objectId);
		
		/* (29.11.2018 TM)NOTE: both exceptions are nothing but debugging checks if the implementation is correct.
		 * In a correct implementation, these exceptions will never occur and are unnecessary code weight.
		 */
	}
	
	private void removeOrphanEntry(final Item[] refHashedItems, final int i)
	{
		int j = i;
		while(++j < refHashedItems.length)
		{
			if(refHashedItems[j] == null)
			{
				break;
			}
		}
		if(j - i == 1)
		{
			// trailing entry just gets nulled out.
			refHashedItems[i] = null;
		}
		else
		{
			// non-trailing entries are removed by shifting the remaining entries one slot forward
			System.arraycopy(refHashedItems, i + 1, refHashedItems, i, j - (i + 1));
		}
	}
	
	private void synchAddInRefKeysBuckets(final long objectId, final Item item)
	{
		// (30.11.2018 TM)XXX: optimizable
		
		// The buckets can never be null. The first 0/null terminates the bucket's entries.
		final Item[] refHashedItems = this.refHashedItemTable[item.hash & this.hashRange];
		for(int i = 0; i < refHashedItems.length; i++)
		{
			if(refHashedItems[i] == null)
			{
				refHashedItems[i] = item;
				return;
			}
			
			// note: the item's referent is strongly referenced from a lower stack frame.
			if(refHashedItems[i].get() == item.get())
			{
				if(refHashedItems[i].objectId != objectId)
				{
					// note: this is a valid exception because it recognized inconsistent data passed from outside.
					throw new PersistenceExceptionConsistencyObjectId(item.get(), refHashedItems[i].objectId, objectId);
				}
				
				// this, however, is just another debugging exception that should never be possible to reach
				// (29.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Instance already registered per-object, but not per-objectId: " + objectId);
			}
		}
		
		// neither inconsistency nor free slot in the bucket, so it must be enlarged to add the new entry
		(this.refHashedItemTable[item.hash & this.hashRange] =
			enlargeBucket(refHashedItems, this.hashDensity))[refHashedItems.length] = item
		;
	}
	
	private static Item[] enlargeBucket(final Item[] bucket, final int increase)
	{
		final Item[] newBucket;
		System.arraycopy(bucket, 0, newBucket = new Item[bucket.length + increase], 0, bucket.length);
		return newBucket;
	}
							
	private void synchCheckForRebuild()
	{
		if(this.size > this.capacityHigh)
		{
//			XDebug.println("Increase required. Size = " + this.size);
			// increase required because the hash table became too small (or entries distribution too dense).
			this.synchRebuild();
			return;
		}
		
		if(this.size < this.capacityLow)
		{
//			XDebug.println("Decrease required. Size = " + this.size);
			// decrease required because the hash table became unnecessarily big. Redundant call for debugging.
			this.synchRebuild();
			return;
		}
		
		// otherwise, no rebuild is required.
	}
	
	
	private void synchRebuild()
	{
		/* Potential Optimization:
		 * Recycling the "refHashed~" bucket arrays would considerable reduce the amount of memory garbage,
		 * but increases the required time by about 5%. The thing with reducing memory garbage is:
		 * After the next GC run, it would be gone, anyway, but a performance loss is permanent.
		 */
		
		// locally cached old references / values.
		final Item[][] oldOidHashedItemTable = this.oidHashedItemTable;
		final int      increase        = this.hashDensity;
		
		// locally created new references / values.
		final int   newHashLength = Hashing.calculateHashLength(this.size, this.hashDensity);
		final int   newHashRange  = newHashLength - 1;
		final Item[][] newRefVals = createRefBucketTable(newHashLength);
		final Item[][] newRefKeys = createRefBucketTable(newHashLength);
		
		// this is also the only opportunity to recalculate the actual size without orphans.
		long size = 0;
		
		// both new branches are populated from the per-oid-branch
		for(int h = 0; h < oldOidHashedItemTable.length; h++)
		{
//			XDebug.println("Rebuild old hash table " + h);
			
			// The buckets can never be null. The first 0/null terminates the bucket's entries.
			final Item[] items = oldOidHashedItemTable[h];
			
			for(int i = 0; i < items.length; i++)
			{
				// looks like this is faster than inlining the check in the loop condition.
				if(items[i] == null)
				{
					break;
				}
				
//				XDebug.println("Rebuild old hash table " + h + "-" + i);
				if(items[i].get() != null)
				{
					registerClean(newRefVals, increase, items[i].objectId, items[i], (int)items[i].objectId & newHashRange);
					registerClean(newRefKeys, increase, items[i].objectId, items[i], items[i].hash & newHashRange);
					size++;
				}
			}
		}
		
//		XDebug.println("Rebuild " + this.hashLength  + " -> " + newHashLength);
		
		// registry state gets switched over from old to new
		this.oidHashedItemTable = newRefVals;
		this.refHashedItemTable = newRefKeys;
		this.hashLength         = newHashLength;
		this.hashRange          = newHashRange;
		this.size               = size;
		// hash density remains the same
		this.internalUpdateCapacities();
		
		// at some point, constant registration is completed, so an efficient storage form is preferable.
		this.synchEnsureConstantsColdStorage();
	}
		
	private static void registerClean(
		final Item[][] refBucketTable,
		final int      bucketIncrease,
		final long     objectId      ,
		final Item     item          ,
		final int      hashIndex
	)
	{
		final Item[] refBucket = refBucketTable[hashIndex];
		for(int i = 0; i < refBucket.length; i++)
		{
			if(refBucket[i] == null)
			{
				refBucket[i] = item    ;
				return;
			}
		}
		
		(refBucketTable[hashIndex] = enlargeBucket(refBucket, bucketIncrease))[refBucket.length] = item    ;
	}
	
	
	// HashStatistics logic //
	
	@Override
	public final synchronized XGettingTable<String, HashStatisticsBucketBased> createHashStatistics()
	{
		return EqHashTable.New(
			KeyValue("PerObjectIds", this.synchCreateHashStatisticsOids()),
			KeyValue("PerObjects", this.synchCreateHashStatisticsRefs())
		);
	}
	
	private HashStatisticsBucketBased synchCreateHashStatisticsOids()
	{
		final EqHashTable<Long, Long> distributionTable = EqHashTable.New();
		
		final Item[][] oidHashedOidKeysTable = this.oidHashedItemTable;
		final int oidHashedOidKeysLength = oidHashedOidKeysTable.length;
		for(int h = 0; h < oidHashedOidKeysLength; h++)
		{
			final Item[] bucket = oidHashedOidKeysTable[h];
			final Long bucketLength = countEntries(bucket);
			registerDistribution(distributionTable, bucketLength);
		}
		complete(distributionTable);
		
		return HashStatisticsBucketBased.New(
			this.hashLength                ,
			this.size                      ,
			this.hashDensity               ,
			distributionTable.keys().last(),
			distributionTable
		);
	}

	private HashStatisticsBucketBased synchCreateHashStatisticsRefs()
	{
		final EqHashTable<Long, Long> distributionTable = EqHashTable.New();

		final Item[][] refHashedRefKeysTable = this.refHashedItemTable;
		final int refHashedRefKeysLength = refHashedRefKeysTable.length;
		for(int h = 0; h < refHashedRefKeysLength; h++)
		{
			final Item[] bucket = refHashedRefKeysTable[h];
			final Long bucketLength = countEntries(bucket);
			registerDistribution(distributionTable, bucketLength);
		}
		complete(distributionTable);
		
		return HashStatisticsBucketBased.New(
			this.hashLength                ,
			this.size                      ,
			this.hashDensity               ,
			distributionTable.keys().last(),
			distributionTable
		);
	}
	
	private static Long countEntries(final Item[] bucket)
	{
		for(int i = bucket.length; --i >= 0;)
		{
			if(bucket[i] != null)
			{
				return i + 1L;
			}
		}
		
		return 0L;
	}
	
	private static void registerDistribution(final EqHashTable<Long, Long> distributionTable, final Long bucketLength)
	{
		// (28.11.2018 TM)NOTE: this is pretty ineffecient. Could be done much more efficient if required.
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
		
		distributionTable.add(null, zero);
		for(long l = 0; l < highest; l++)
		{
			distributionTable.add(l, zero);
		}
		distributionTable.keys().sort(XSort::compare);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// member types     //
	/////////////////////
	
	static final class Item extends WeakReference<Object>
	{
		final long objectId;
		int hash;
		
		Item(final long objectId, final Object referent)
		{
			super(referent);
			this.objectId = objectId;
			this.hash     = System.identityHashCode(referent);
		}
	}
	
}