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
import net.jadoth.math.XMath;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObject;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceAcceptor;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.typing.KeyValue;


public final class DefaultObjectRegistry implements PersistenceObjectRegistry
{
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
		
	public static final float defaultHashDensity()
	{
		// below that, the overhead for the quad-bucket-arrays does not pay off.
		return 8.0f;
	}
	
	
	public static DefaultObjectRegistry New()
	{
		return New(defaultHashDensity());
	}
	
	public static DefaultObjectRegistry New(final float desiredHashDensity)
	{
		return New(
			desiredHashDensity,
			(int)Math.ceil(desiredHashDensity / 2) + 1
		);
	}
	
	public static DefaultObjectRegistry New(
		final float desiredHashDensity,
		final int   bucketLengthInitialAndIncrease
	)
	{
		return New(
			desiredHashDensity            ,
			bucketLengthInitialAndIncrease,
			bucketLengthInitialAndIncrease
		);
	}
	
	public static DefaultObjectRegistry New(
		final float desiredHashDensity  ,
		final int   bucketLengthInitial ,
		final int   bucketLengthIncrease
	)
	{
		// there is no point in supporting a desired initial capacity when there's a capacity low bound for the size.
		
		return new DefaultObjectRegistry()
			.internalSetHashConfiguration(
				Hashing.hashDensity(desiredHashDensity),
				XMath.positive(bucketLengthInitial),
				XMath.positive(bucketLengthIncrease)
			)
			.internalReset()
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private long[][] oidHashedOidKeysTable;
	private Item[][] oidHashedRefValsTable;
	private Item[][] refHashedRefKeysTable;
	private long[][] refHashedOidValsTable;
	
	/**
	 * A measurement of how "dense" entries are packed in the hashing structure.<br>
	 * Higher density means less memory consumption but also lower performance.<br>
	 * See {@link #minimumHashDensity()}.
	 */
	private float hashDensity;
	
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
	
	private int bucketLengthInitial;
	
	private int bucketLengthIncrease;
	
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
		// staring low makes no performance difference in the long run.
		return this.internalReset(1);
	}
	
	final DefaultObjectRegistry internalReset(final int hashLength)
	{
		this.oidHashedOidKeysTable = new long[hashLength][];
		this.oidHashedRefValsTable = new Item[hashLength][];
		this.refHashedRefKeysTable = new Item[hashLength][];
		this.refHashedOidValsTable = new long[hashLength][];
		
		this.hashLength = hashLength;
		this.hashRange  = hashLength - 1;
		
		this.internalUpdateCapacities();
		
		this.size = 0;
		
		return this;
	}
	
	final DefaultObjectRegistry internalSetHashDensity(final float hashDensity)
	{
		this.hashDensity = hashDensity;
		
		return this;
	}
	
	final DefaultObjectRegistry internalSetHashConfiguration(
		final float hashDensity         ,
		final int   bucketLengthInitial ,
		final int   bucketLengthIncrease
		
	)
	{
		this.hashDensity          = hashDensity         ;
		this.bucketLengthInitial  = bucketLengthInitial ;
		this.bucketLengthIncrease = bucketLengthIncrease;
		
		return this;
	}
	
	private void internalUpdateCapacities()
	{
		this.capacityHigh = (long)(this.hashLength * this.hashDensity);
		this.capacityLow  = this.hashLength == 1
			? 0
			: (long)(this.hashLength / 2 * this.hashDensity)
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
		this.internalSetHashDensity(Hashing.hashDensity(hashDensity));
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
		notNull(object);
		
		final Item[] refHashedRefKeys;
		if((refHashedRefKeys = this.refHashedRefKeysTable[this.hash(object)]) == null)
		{
			return Persistence.nullId();
		}
		
		/* Potential Optimization:
		 * Only lock while the oidKeys bucket is queries.
		 * Could be reliably thread-local after that.
		 * (not exactely sure about the "reliably". Would have to be researched/tested)
		 */
		
		/* Potential Optimization:
		 * Quick-check at index 0, since buckets never have a length of 0.
		 * Performance-gain must be tested, first, though.
		 */
		
		// the array should be a concurrency-safe thread-local stack copy, but honestly not exactely sure about it.
		for(int i = 0; i < refHashedRefKeys.length; i++)
		{
			// the first null terminates the bucket's entries
			if(refHashedRefKeys[i] == null)
			{
				break;
			}
			
			// can be null for orphan entries
			if(refHashedRefKeys[i].get() == object)
			{
				// lookup and return the associated objectId (always located at the same index in its bucket)
				// (29.11.2018 TM)XXX: test and comment if rehashing is faster than "refHashedRefKeys[i].refHashIndex".
				return this.refHashedOidValsTable[refHashedRefKeys[i].refHashIndex][i];
			}
		}
		
		// since null can never be contained, returning the null-id signals a miss.
		return Persistence.nullId();
	}
	
	@Override
	public final synchronized Object lookupObject(final long objectId)
	{
		final long[] oidHashedOidKeys;
		if((oidHashedOidKeys = this.oidHashedOidKeysTable[this.hash(objectId)]) == null)
		{
			// since null can never be contained, returning the null signals a miss.
			return null;
		}
		
		/* Potential Optimization:
		 * Only lock while the oidKeys bucket is queries.
		 * Could be reliably thread-local after that.
		 * (not exactely sure about the "reliably". Would have to be researched/tested)
		 */
		
		/* Potential Optimization:
		 * Quick-check at index 0, since buckets never have a length of 0.
		 * Performance-gain must be tested, first, though.
		 */

		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			// the first 0 terminates the bucket's entries.
			if(oidHashedOidKeys[i] == 0L)
			{
				break;
			}
			
			if(oidHashedOidKeys[i] == objectId)
			{
				return this.oidHashedRefValsTable[this.hash(objectId)][i].get();
			}
		}
		
		// since null can never be contained, returning the null signals a miss.
		return null;
	}
	
	@Override
	public final synchronized boolean containsObjectId(final long objectId)
	{
		final long[] oidHashedOidKeys;
		if((oidHashedOidKeys = this.oidHashedOidKeysTable[this.hash(objectId)]) == null)
		{
			return false;
		}
		
		/* Potential Optimization:
		 * Only lock while the oidKeys bucket is queries.
		 * Could be reliably thread-local after that.
		 * (not exactely sure about the "reliably". Would have to be researched/tested)
		 */
		
		/* Potential Optimization:
		 * Quick-check at index 0, since buckets never have a length of 0.
		 * Performance-gain must be tested, first, though.
		 */

		// the array should be a concurrency-safe thread-local stack copy, but honestly not exactely sure about it.
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			// the first 0 terminates the bucket's entries
			if(oidHashedOidKeys[i] == 0L)
			{
				break;
			}
			
			if(oidHashedOidKeys[i] == objectId)
			{
				// signal objectId found.
				return true;
			}
		}
		
		// signal objectId not found.
		return false;
	}
	
	
	
	@Override
	public final synchronized <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		// iterating everything has so many bucket accesses that a total lock is the only viable thing to do.
		
		final long[][] oidHashedOidKeysTable = this.oidHashedOidKeysTable;
		final Item[][] oidHashedRefValsTable = this.oidHashedRefValsTable;
		
		final int oidHashedOidKeysLength = oidHashedOidKeysTable.length;
		
		for(int h = 0; h < oidHashedOidKeysLength; h++)
		{
			final long[] oidHashedOidKeys = oidHashedOidKeysTable[h];
			if(oidHashedOidKeys == null)
			{
				continue;
			}
			
			final Item[] oidHashedRefVals = oidHashedRefValsTable[h];
			
			for(int i = 0; i < oidHashedOidKeys.length; i++)
			{
				// the first null terminates the bucket's entries
				if(oidHashedRefVals[i] == null)
				{
					break;
				}
				
				// orphan entries are passed intentionally to give this method a usage as an orphan analyzing tool.
				acceptor.accept(oidHashedOidKeys[i], oidHashedRefVals[i].get());
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
		
		return this.constantsHotRegistry;
	}
	
	private void synchEnsureConstantColdStorage()
	{
		if(this.constantsColdStorageObjects != null)
		{
			return;
		}
		
		final EqHashTable<Long, Object> constantsHotRegistry = this.constantsHotRegistry;
		final int                       constantCount        = X.checkArrayRange(constantsHotRegistry.size());
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
		this.synchRebuild();
	}
	
	@Override
	public final synchronized void clear()
	{
		// reinitialize storage strucuture with a suitable size for the incoming constants.
		this.synchEnsureConstantColdStorage();
		
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

	
	
	private Object synchRegisterObject(final long objectId, final Object object, final boolean optional)
	{
		// (29.11.2018 TM)XXX: test and comment if optional flag removal improved performance
		
		int i = -1;
		final long[] oidKeys;
		final Item[] refVals;
		
		if((oidKeys = this.oidHashedOidKeysTable[this.hash(objectId)]) != null)
		{
			// case 1: existing buckets, scroll to adding index while checking for a matching entry.
			refVals = this.oidHashedRefValsTable[this.hash(objectId)];
			while(++i < oidKeys.length && refVals[i] != null)
			{
				if(oidKeys[i] == objectId)
				{
					return this.synchHandleOidKeyBucketsMatch(refVals, i, objectId, object, optional);
				}
			}
		}
		else
		{
			// case 2: no oidKeys bucket, so the object CANNOT be registered, yet and will be added in new buckets.
			refVals = null;
		}
		
		// add in oid buckets, either new ones or existing ones or enlarged ones.
		this.synchAddInOidKeyBuckets(oidKeys, refVals, objectId, object, i);
		return object;
	}
					
	private Object synchHandleOidKeyBucketsMatch(
		final Item[]  refVals ,
		final int     i       ,
		final long    objectId,
		final Object  object  ,
		final boolean optional
	)
	{
		final Object alreadyRegistered;

		// case 1: object is already consistently registered, abort.
		if((alreadyRegistered = refVals[i].get()) == object)
		{
			return null;
		}

		// case 2: matching entry, but orphaned. Can and MUST be reused for a new Item.
		if(alreadyRegistered == null)
		{
			this.synchReplaceOrphanEntry(refVals[i], objectId, refVals[i] = new Item(object, this.hash(objectId), this.hash(object)));
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
	
	private void synchAddInOidKeyBuckets(
		final long[] oidKeys ,
		final Item[] refVals ,
		final long   objectId,
		final Object object  ,
		final int    i
	)
	{
		final Item item = new Item(object, this.hash(objectId), this.hash(object));
		
		this.synchAddInRefKeysBuckets(objectId, item);
		
		if(oidKeys == null)
		{
			// case 1: no buckets, yet. New ones are created with the entry at the beginning.
			setEntry(
				this.oidHashedOidKeysTable[item.oidHashIndex] = new long[this.bucketLengthInitial],
				this.oidHashedRefValsTable[item.oidHashIndex] = new Item[this.bucketLengthInitial],
				objectId,
				item,
				0 // always index 0 (also, i is inconsistently -1 here as a performance optimization).
			);
		}
		else if(i < oidKeys.length)
		{
			// case 2: current buckets have a free slot.
			setEntry(oidKeys, refVals, objectId, item, i);
		}
		else
		{
			// case 3: existing buckets must be enlarged
			setEntry(
				this.oidHashedOidKeysTable[item.oidHashIndex] = enlargeBucket(oidKeys, this.bucketLengthIncrease),
				this.oidHashedRefValsTable[item.oidHashIndex] = enlargeBucket(refVals, this.bucketLengthIncrease),
				objectId,
				item,
				i
			);
		}
		
		this.size++;
	}
					
	private void synchReplaceOrphanEntry(final Item orphanItem, final long objectId, final Item newItem)
	{
		final Item[] refHashedRefKeys = this.refHashedRefKeysTable[orphanItem.refHashIndex];
		for(int i = 0; i < refHashedRefKeys.length; i++)
		{
			if(refHashedRefKeys[i] != orphanItem)
			{
				continue;
			}

			final long[] refHashedOidVals = this.refHashedOidValsTable[orphanItem.refHashIndex];
			if(refHashedOidVals[i] != objectId)
			{
				// (29.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Orphan entry object id inconsistency: " + refHashedOidVals[i] + " != " + objectId);
			}
			
			this.removeOrphanEntry(refHashedRefKeys, refHashedOidVals, i);
			this.synchAddInRefKeysBuckets(objectId, newItem);
			return;
		}

		// (29.11.2018 TM)EXCP: proper exception
		throw new RuntimeException("Orphan entry not found for object id " + objectId);
		
		/* (29.11.2018 TM)NOTE: both exceptions are nothing but debugging checks if the implementation is correct.
		 * In a correct implementation, these exceptions will never occur and are unnecessary code weight.
		 */
	}
	
	private void removeOrphanEntry(final Item[] refHashedRefKeys, final long[] refHashedOidVals, final int i)
	{
		int j = i;
		while(++j < refHashedRefKeys.length)
		{
			if(refHashedRefKeys[j] == null)
			{
				break;
			}
		}
		if(j - i == 1)
		{
			// trailing entry just gets nulled out.
			refHashedRefKeys[i] = null;
			refHashedOidVals[i] = Persistence.nullId();
		}
		else
		{
			// non-trailing entries are removed by shifting the remaining entries one slot forward
			System.arraycopy(refHashedRefKeys, i + 1, refHashedRefKeys, i, j - (i + 1));
			System.arraycopy(refHashedOidVals, i + 1, refHashedOidVals, i, j - (i + 1));
		}
	}
	
	private void synchAddInRefKeysBuckets(final long objectId, final Item item)
	{
		final Item[] refKeys = this.refHashedRefKeysTable[item.refHashIndex];
		if(refKeys == null)
		{
			setEntry(
				this.refHashedOidValsTable[item.refHashIndex] = new long[this.bucketLengthInitial],
				this.refHashedRefKeysTable[item.refHashIndex] = new Item[this.bucketLengthInitial],
				objectId,
				item,
				0
			);
			return;
		}

		final long[] oidVals = this.refHashedOidValsTable[item.refHashIndex];
		for(int i = 0; i < refKeys.length; i++)
		{
			if(refKeys[i] == null)
			{
				setEntry(oidVals, refKeys, objectId, item, i);
				return;
			}
			
			// note: the item's referent is strongly referenced from a lower stack frame.
			if(refKeys[i].get() == item.get())
			{
				if(oidVals[i] != objectId)
				{
					// note: this is a valid exception because it recognized inconsistent data passed from outside.
					throw new PersistenceExceptionConsistencyObjectId(item.get(), oidVals[i], objectId);
				}
				
				// this, however, is just another debugging exception that should never be possible to reach
				// (29.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Instance already registered per-object, but not per-objectId: " + objectId);
			}
		}
		
		// neither inconsistency nore free slot in the bucket, so it must be enlarged to add the new entry
		setEntry(
			this.refHashedOidValsTable[item.refHashIndex] = enlargeBucket(oidVals, this.bucketLengthIncrease),
			this.refHashedRefKeysTable[item.refHashIndex] = enlargeBucket(refKeys, this.bucketLengthIncrease),
			objectId,
			item,
			refKeys.length
		);
	}
		
	private static long[] enlargeBucket(final long[] bucket, final int increase)
	{
		final long[] newBucket;
		System.arraycopy(bucket, 0, newBucket = new long[bucket.length + increase], 0, bucket.length);
		return newBucket;
	}
	
	private static Item[] enlargeBucket(final Item[] bucket, final int increase)
	{
		final Item[] newBucket;
		System.arraycopy(bucket, 0, newBucket = new Item[bucket.length + increase], 0, bucket.length);
		return newBucket;
	}
	
	private static void setEntry(
		final long[] oidBucket,
		final Item[] refBucket,
		final long   objectId ,
		final Item   item     ,
		final int    i
	)
	{
		oidBucket[i] = objectId;
		refBucket[i] = item;
	}
						
	private void synchCheckForRebuild()
	{
		if(this.size > this.capacityHigh)
		{
//			XDebug.println("Increase required.");
			// increase required because the hash table became too small (or entries distribution too dense).
			this.synchRebuild();
			return;
		}
		
		if(this.size < this.capacityLow)
		{
//			XDebug.println("Decrease required.");
			// decrease required because the hash table became unnecessarily big. Redundant call for debugging.
			this.synchRebuild();
			return;
		}
		
		// otherwise, no rebuild is required.
	}
	
	private void synchRebuild()
	{
		/* Potential Optimization:
		 * "refHashed~" arrays could be recycled. Already processed "oidHashed~" arrays, too.
		 * However, it would have to be tested if that really brings performance or just
		 * eases garbage collection, maybe even at the cost of performence.
		 */
		
		// locally cached old references / values.
		final int      oldHashLength   = this.hashLength;
		final long[][] oldOidKeysTable = this.oidHashedOidKeysTable;
		final Item[][] oldRefValsTable = this.oidHashedRefValsTable;
		final int      increase  = this.bucketLengthIncrease;
		
		// locally created new references / values.
		final int   newHashLength = Hashing.calculateHashLength(this.size, this.hashDensity);
		final int   newHashRange  = newHashLength - 1;
		final long[][] newOidKeys = new long[newHashLength][this.bucketLengthInitial];
		final Item[][] newRefVals = new Item[newHashLength][this.bucketLengthInitial];
		final Item[][] newRefKeys = new Item[newHashLength][this.bucketLengthInitial];
		final long[][] newOidVals = new long[newHashLength][this.bucketLengthInitial];
		
		// this is also the only opportunity to recalculate the actual size
		long size = 0;
		
		// both new branches are populated from the per-oid-branch
		for(int h = 0; h < oldHashLength; h++)
		{
//			XDebug.println("Rebuild old hash table " + h);
			final long[] oldOidKeys = oldOidKeysTable[h];
			if(oldOidKeys == null)
			{
				continue;
			}
			
			final Item[] oldRefVals = oldRefValsTable[h];
			for(int i = 0; i < oldRefVals.length; i++)
			{
//				XDebug.println("Rebuild old hash table " + h + " -> " + i);
				final Object object;
				if((object = oldRefVals[i].get()) == null)
				{
//					XDebug.println("Orphan");
					continue;
				}
				
				final long oid  = oldOidKeys[i];
				final Item item = oldRefVals[i];
				register(newOidKeys, newRefVals, increase, oid, item, item.oidHashIndex = (int)oid & newHashRange);
				register(newOidVals, newRefKeys, increase, oid, item, item.refHashIndex = System.identityHashCode(object) & newHashRange);
				size++;
				
//				XDebug.println("new Entry: " + size);
			}
		}
		
//		XDebug.println("Rebuild " + this.hashLength  + " -> " + newHashLength);
		
		// registry state gets switched over from old to new
		this.oidHashedOidKeysTable = newOidKeys;
		this.oidHashedRefValsTable = newRefVals;
		this.refHashedRefKeysTable = newRefKeys;
		this.refHashedOidValsTable = newOidVals;
		this.hashLength            = newHashLength;
		this.hashRange             = newHashRange;
		this.size                  = size ;
		// hash density remains the same
		this.internalUpdateCapacities();
		
		// at some point, constant registration is completed, so an efficient storage form is preferable.
		this.synchEnsureConstantColdStorage();
	}
	
	private static void register(
		final long[][] oidBucketTable,
		final Item[][] refBucketTable,
		final int      bucketIncrease,
		final long     objectId      ,
		final Item     item          ,
		final int      hashIndex
	)
	{
		final long[] oidBucket = oidBucketTable[hashIndex];
		final Item[] refBucket = refBucketTable[hashIndex];
		
		for(int i = 0; i < refBucket.length; i++)
		{
			if(refBucket[i] == null)
			{
				oidBucket[i] = objectId;
				refBucket[i] = item    ;
				return;
			}
		}
		
		(oidBucketTable[hashIndex] = enlargeBucket(oidBucket, bucketIncrease))[refBucket.length] = objectId;
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
		
		final long[][] oidHashedOidKeysTable = this.oidHashedOidKeysTable;
		final int oidHashedOidKeysLength = oidHashedOidKeysTable.length;
		for(int h = 0; h < oidHashedOidKeysLength; h++)
		{
			final long[] bucket = oidHashedOidKeysTable[h];
			final Long bucketLength = bucket == null ? null : (long)bucket.length;
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

		final Item[][] refHashedRefKeysTable = this.refHashedRefKeysTable;
		final int refHashedRefKeysLength = refHashedRefKeysTable.length;
		for(int h = 0; h < refHashedRefKeysLength; h++)
		{
			final Item[] bucket = refHashedRefKeysTable[h];
			final Long bucketLength = bucket == null ? null : (long)bucket.length;
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
		int oidHashIndex; // technically not necessary, but 8-byte memory padding leaves room for it, anyway.
		int refHashIndex;
		
		Item(final Object referent, final int oidHashIndex, final int refHashIndex)
		{
			super(referent);
			this.oidHashIndex = oidHashIndex;
			this.refHashIndex = refHashIndex;
		}
	}
	
}