package net.jadoth.persistence.internal;

import static java.lang.System.identityHashCode;
import static net.jadoth.X.KeyValue;

import java.lang.ref.WeakReference;

import net.jadoth.X;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hashing.HashStatisticsBucketBased;
import net.jadoth.hashing.XHashing;
import net.jadoth.math.XMath;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import net.jadoth.persistence.exceptions.PersistenceExceptionInvalidObjectRegistryCapacity;
import net.jadoth.persistence.exceptions.PersistenceExceptionNullObjectId;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceAcceptor;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.typing.KeyValue;

public final class DefaultObjectRegistry implements PersistenceObjectRegistry
{
	/* (27.11.2018 TM)TODO: ObjectRegistry housekeeping thread
	 * - tryCleanUp() method to random-sample-check for trimmable buckets arrays (empty buckets and/or orphans)
	 * - thread with weak back-reference to this registry to make it stop automatically.
	 * - "lastRegister" timestamp to not interrupt registering-heavy phases.
	 * - some special methods like ensureCapacity must set the timestamp, too, to avoid counterproductive behavior.
	 * - the usual config values for check intervals, sample size, etc.
	 * - start() and stop() method in the registry for explicit control.
	 * - a size increase ensures the thread is running, any clear/truncate terminates it.
	 */
	
	/* Funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * Welcome to this user code class.
	 */
	
	/* Notes on byte size per entry (+/- COOPS):
	 * - Entry instances occupy 40/64 bytes, including memory padding (12/16 bytes header, 4 references, 1 long, 1 int)
	 * - The hash density specifies the average length of the buckets array in a hash table slot.
	 * - Arrays have a header length of 16/24 bytes.
	 * - All entries in one buckets array share the array's total length and the two hash table slots.
	 * 
	 * Conclusion:
	 * The major memory eater is the rather big JDK WeakReference implementation.
	 * Increasing the hash density up to 16.0 can be reasonable to minimize the implementation's memory consumption.
	 * Blame the rest on the JDK.
	 * 
	 * Hash   |  bytes/entry  | Perf.
	 * density| +COOPS -COOPS |estim.
	 * ------------------------------
	 * ^ anything above is ridiculous
	 *  16,00 |  57,50  84,00 | ~ 55%
	 *   8,00 |  59,00  88,00 | ~ 65%
	 *   4,00 |  62,00  96,00 | ~ 70%
	 *   3,20 |  63,50 100,00 | ~ 75%
	 *   2,00 |  68,00 112,00 | ~ 80%
	 *   1,60 |  71,00 120,00 | ~ 85%
	 *   1,00 |  80,00 144,00 | =100%
	 *   0,80 |  86,00 160,00 | ~105%
	 *   0,50 | 104,00 208,00 | ~110%
	 * v anything below is ridiculous
	 */

	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

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
	 * Note on hashDensity:
	 * Values < 1.0 are hardly worth it.
	 * Values from 1.6 to 8.0 can be a reasonable trade of less memory consumption for acceptably lower performance.
	 * Anything above 16.0 or below 0.5 is ridiculous.
	 * 
	 * @param slotSize
	 * @param hashDensity.
	 * @return
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
	
	private Entry[][] oidHashTable;
	private Entry[][] refHashTable;
	private int       hashRange   ; // bit mask / modulo value used for hashing.
	private float     hashDensity ; // average number of buckets per hash table slot for well-distributed hash values.
	private long      capacity    ; // upper rebuild threshold.
	private long      minCapacity ; // minimum capacity
	private long      size        ;
	
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
	
	private int internalHashLength()
	{
		return this.hashRange + 1;
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
	
	private void internalSetHashDensity(final float validHashDensity)
	{
		this.hashDensity = validHashDensity;
	}
	
	private void internalSetMinimumCapacity(final long validMinimumCapacity)
	{
		this.minCapacity = validMinimumCapacity;
	}
	
	@Override
	public synchronized boolean setHashDensity(final float hashDensity)
	{
		this.internalSetHashDensity(validateHashDensity(hashDensity));
		
		this.internalUpdateCapacities();
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
		
		this.internalUpdateCapacities();
		return this.ensureCapacity(minimumCapacity);
	}
	
	@Override
	public final synchronized boolean setMinimumCapacity(final long minimumCapacity)
	{
		this.internalSetMinimumCapacity(validateCapacity(minimumCapacity));
		
		this.internalUpdateCapacities();
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
			this.synchRebuild(requiredHashLength);
			
			return true;
		}
		
		return false;
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
		
	private void setHashTables(final Entry[][] oidHashTable, final Entry[][] refHashTable)
	{
		this.oidHashTable = oidHashTable;
		this.refHashTable = refHashTable;
		this.hashRange    = oidHashTable.length - 1;
		this.internalUpdateCapacities();
	}
		
	private Entry[][] createHashTable(final int hashLength)
	{
		return new Entry[hashLength][(int)Math.ceil(this.hashDensity)];
	}
	
	private void internalUpdateCapacities()
	{
		this.capacity = this.internalHashLength() >= XMath.highestPowerOf2_int()
			? Long.MAX_VALUE
			: (long)(this.internalHashLength() * this.hashDensity)
		;
	}

	private synchronized boolean synchronizedAdd(final long objectId, final Object object)
	{
		if(this.synchAddCheck(objectId, object))
		{
			return false;
		}

		synchInsertEntry(this.oidHashTable, this.refHashTable, new Entry(objectId, object), this.hashRange);

		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		return true;
	}
	
	private boolean synchAddCheck(final long objectId, final Object object)
	{
		final Entry[] oidBuckets;
		if((oidBuckets = this.oidHashTable[(int)objectId & this.hashRange]) != null)
		{
			for(int i = 0; i < oidBuckets.length; i++)
			{
				if(oidBuckets[i] != null && oidBuckets[i].objectId == objectId)
				{
					this.synchHandleExisting(i, objectId, object, oidBuckets[i]);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void synchHandleExisting(
		final int    oidIndex,
		final long   oid     ,
		final Object object  ,
		final Entry  entry
	)
	{
		if(entry.get() == object)
		{
			return;
		}

		validateReferenceNotYetRegistered(this.refHashTable[identityHashCode(object) & this.hashRange], oid, object);

		remove(this.refHashTable[entry.refHash & this.hashRange], entry);

		final Entry newEntry = this.oidHashTable[(int)oid & this.hashRange][oidIndex] = new Entry(oid, object);
		synchPutEntry(this.refHashTable, newEntry.refHash & this.hashRange, newEntry);
	}
	
	private static void validateReferenceNotYetRegistered(
		final Entry[] refBuckets,
		final long    objectId  ,
		final Object  object
	)
	{
		if(refBuckets != null)
		{
			for(int i = 0; i < refBuckets.length; i++)
			{
				if(refBuckets[i] != null && refBuckets[i].get() == object)
				{
					validateEntryOid(refBuckets[i], objectId, object);
					return;
				}
			}
		}
	}
	
	private static void remove(final Entry[] buckets, final Entry entry)
	{
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] == entry)
			{
				buckets[i] = null;
				return;
			}
		}
	}

	private synchronized Object synchronizedAddGet(final long objectId, final Object object)
	{
		final Object alreadyRegistered;
		if((alreadyRegistered = this.synchAddGetCheck(objectId, object)) != null)
		{
			return alreadyRegistered;
		}

		synchInsertEntry(this.oidHashTable, this.refHashTable, new Entry(objectId, object), this.hashRange);

		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		return object;
	}
	
	private Object synchAddGetCheck(final long objectId, final Object object)
	{
		final Entry[] oidBuckets;
		if((oidBuckets = this.oidHashTable[(int)objectId & this.hashRange]) != null)
		{
			for(int i = 0; i < oidBuckets.length; i++)
			{
				if(oidBuckets[i] != null && oidBuckets[i].objectId == objectId)
				{
					final Object registered;
					if((registered = oidBuckets[i].get()) != null)
					{
						return registered == object
							? null
							: registered
						;
					}
					this.synchHandleExisting(i, objectId, registered, oidBuckets[i]);
					return true;
				}
			}
		}
		
		return false;
	}

	private void synchIncreaseStorage()
	{
		// capacityHighBound checks prevent unnecessary / dangerous calls of this method
		this.synchRebuild(this.oidHashTable.length << 1);
	}
	
	private void synchRebuild(final int hashLength)
	{
		final Entry[][] newSlotsPerOid = this.createHashTable(hashLength);
		final Entry[][] newSlotsPerRef = this.createHashTable(hashLength);

		synchRebuildSlots(this.oidHashTable, newSlotsPerOid, newSlotsPerRef);

		this.setHashTables(newSlotsPerOid, newSlotsPerRef);
		
		// at some point, constant registration is completed, so an efficient storage form is preferable.
		this.synchEnsureConstantsColdStorage();
	}

	@Override
	public synchronized boolean consolidate()
	{
		// both tables always have the same length
		final Entry[][] oidHashTable = this.oidHashTable;
		final Entry[][] refHashTable = this.refHashTable;
		
		int size = 0;
		for(int h = 0; h < oidHashTable.length; h++)
		{
			// the primary branch (per oids) is used to determine the size
			size += synchCleanUpBucket(oidHashTable, h);
			
			// the secondard branch is just updated
			synchCleanUpBucket(refHashTable, h);
		}
		
		this.size = size;
		
		return this.synchCheckForDecrease();
	}
	
	private static int synchCleanUpBucket(final Entry[][] hashTable, final int h)
	{
		final Entry[] buckets = hashTable[h];
		if(buckets == null)
		{
			return 0;
		}
		
		int entryCount = 0;
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] != null && buckets[i].get() != null)
			{
				entryCount++;
			}
		}
		
		if(entryCount == 0)
		{
			hashTable[h] = null;
			return 0;
		}
		
		final Entry[] newBuckets = new Entry[entryCount];
		int t = 0;
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] != null && buckets[i].get() != null)
			{
				newBuckets[t++] = buckets[i];
			}
		}
		
		return t;
	}
	
	private boolean synchCheckForDecrease()
	{
		final int requiredHashLength = this.calculateRequiredHashLength(this.size);
		if(requiredHashLength != this.internalHashLength())
		{
			this.synchRebuild(requiredHashLength);
			
			return true;
		}
		
		return false;
	}

	private static long lookupOid(final Entry[] buckets, final Object object)
	{
		if(buckets != null)
		{
			for(int i = 0; i < buckets.length; i++)
			{
				if(buckets[i] != null && buckets[i].get() == object)
				{
					return buckets[i].objectId;
				}
			}
		}
		
		return Persistence.nullId();
	}

	private static Object lookupObject(final Entry[] buckets, final long oid)
	{
		if(buckets != null)
		{
			for(int i = 0; i < buckets.length; i++)
			{
				if(buckets[i] != null && buckets[i].objectId == oid)
				{
					return buckets[i].get();
				}
			}
		}
				
		return null;
	}

	private static boolean containsOid(final Entry[] buckets, final long oid)
	{
		if(buckets != null)
		{
			for(int i = 0; i < buckets.length; i++)
			{
				if(buckets[i] != null && buckets[i].objectId == oid)
				{
					return true;
				}
			}
		}
		return false;
	}


	private static void validateEntryOid(final Entry entry, final long objectId, final Object object)
	{
		if(entry.objectId != objectId)
		{
			throw new PersistenceExceptionConsistencyObjectId(object, entry.objectId, objectId);
		}
	}

	private static Entry[] synchEnlargeBuckets(final Entry[] oldBuckets, final Entry entry)
	{
		/* Notes:
		 * - orphan entries are kept intentionally
		 * - bucket length is very unlikely to grow beyond max pow2 int, so no grow length check is performed
		 * - an increment of 4 proved to be highly efficient for reasonable hash densities.
		 */
		final Entry[] newBuckets;
		System.arraycopy(oldBuckets, 0, newBuckets = new Entry[oldBuckets.length + 4], 0, oldBuckets.length);
		newBuckets[oldBuckets.length] = entry;
		return newBuckets;
	}

	private static boolean synchUseEmptyBucket(final Entry[] buckets, final Entry entry)
	{
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] == null)
			{
				buckets[i] = entry;
				return true;
			}
		}
		return false;
	}

	private static void synchRemoveEntry(final Entry[] buckets, final Entry entry)
	{
		if(buckets == null)
		{
			// can happen via rebuild (e.g. clean up).
			return;
		}
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] == entry)
			{
				buckets[i] = null;
				return;
			}
		}
	}

	private static void synchInsertEntry(final Entry[][] oidHashTable, final Entry[][] refHashTable, final Entry entry)
	{
		synchPutEntry(oidHashTable, (int)entry.objectId & oidHashTable.length - 1, entry);
		synchPutEntry(refHashTable,      entry.refHash  & oidHashTable.length - 1, entry);
	}
	
	// The parameter order significantly influences performance. Do not change.
	private static void synchInsertEntry(
		final Entry[][] slotsPerOid,
		final Entry[][] slotsPerRef,
		final Entry     entry      ,
		final int       hashRange
	)
	{
		synchPutEntry(slotsPerOid, hashRange & (int)entry.objectId, entry);
		synchPutEntry(slotsPerRef, hashRange &      entry.refHash , entry);
	}

	private static Entry[] synchCreateNewBuckets(final Entry entry)
	{
		/* starting at only one bucket is a little inefficient for enlargement but pretty memory-efficient.
		 * As the goal is to produce as few collisions as possible, single-entry buckets is the common case.
		 */
		return new Entry[]{entry};
	}

	private static void synchPutEntry(final Entry[][] table, final int hashIndex, final Entry entry)
	{
		// clean up removed empty bucket arrays to reduce memory consumption
		if(table[hashIndex] == null)
		{
			// case 1: slot still empty. Create new buckets array.
			table[hashIndex] = synchCreateNewBuckets(entry);
		}
		else if(!synchUseEmptyBucket(table[hashIndex], entry)) // case 2: buckets array still has an empty bucket.
		{
			// case 3: buckets array full. Enlarge and insert.
			table[hashIndex] = synchEnlargeBuckets(table[hashIndex], entry);
		}
	}

	private static void synchRebuildSlots(
		final Entry[][] oldSlots      ,
		final Entry[][] newSlotsPerOid,
		final Entry[][] newSlotsPerRef
	)
	{
		for(int i = 0; i < oldSlots.length; i++)
		{
			if(oldSlots[i] != null)
			{
				synchRebuildBuckets(oldSlots[i], newSlotsPerOid, newSlotsPerRef);
			}
		}
	}

	private static void synchRebuildBuckets(
		final Entry[]   buckets       ,
		final Entry[][] newSlotsPerOid,
		final Entry[][] newSlotsPerRef
	)
	{
		for(int i = 0; i < buckets.length; i++)
		{
			if(buckets[i] != null && !buckets[i].isEmpty())
			{
				synchInsertEntry(newSlotsPerOid, newSlotsPerRef, buckets[i]);
			}
		}
	}

	private static void iterateEntries(final Entry[][] slots, final PersistenceAcceptor acceptor)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null)
					{
						acceptor.accept(buckets[b].objectId, buckets[b].get());
					}
				}
			}
		}
	}
	
	@Override
	public final synchronized void clear()
	{
		this.synchEnsureConstantsColdStorage();
		this.internalClear();
		this.reregisterConstants();
	}
	
	@Override
	public final synchronized void clearAll()
	{
		this.internalClear();
	}
	
	private void internalClear()
	{
		final Entry[][]
			oidBuckets = this.oidHashTable,
			refBuckets = this.refHashTable
		;
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
		this.synchEnsureConstantsColdStorage();
		
		this.internalReset(Math.max(this.constantsColdStorageObjects.length, this.minCapacity));
		
		this.reregisterConstants();
	}
	
	@Override
	public final synchronized void truncateAll()
	{
		// hash table reset, no constants reregistering.
		this.internalReset();
	}

	@Override
	public final synchronized boolean containsObjectId(final long oid)
	{
		return containsOid(this.oidHashTable[(int)oid & this.hashRange], oid);
	}
	
	private void reregisterConstants()
	{
		final Object[] constantsObjects = this.constantsColdStorageObjects;
		final long[] constantsObjectIds = this.constantsColdStorageObjectIds;
		
		for(int i = 0; i < constantsObjects.length; i++)
		{
			// NOT registerConstant() at this point!
			this.registerObject(constantsObjectIds[i], constantsObjects[i]);
		}
	}

	@Override
	public final synchronized long lookupObjectId(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		
		return lookupOid(this.refHashTable[identityHashCode(object) & this.hashRange], object);
	}

	@Override
	public final synchronized Object lookupObject(final long oid)
	{
		return lookupObject(this.oidHashTable[(int)oid & this.hashRange], oid);
	}

	@Override
	public final boolean registerObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}

		return this.synchronizedAdd(oid, object);
	}

	@Override
	public Object optionalRegisterObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}
		
		return this.synchronizedAddGet(oid, object);
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

	public final synchronized boolean removeObjectById(final long id)
	{
		if(id == Persistence.nullId())
		{
			return false;
		}
		
		final Entry[] oidBuckets;
		if((oidBuckets = this.oidHashTable[(int)id & this.hashRange]) != null)
		{
			for(int i = 0; i < oidBuckets.length; i++)
			{
				if(oidBuckets[i] != null && oidBuckets[i].objectId == id)
				{
					synchRemoveEntry(this.refHashTable[oidBuckets[i].refHash & this.hashRange], oidBuckets[i]);
					oidBuckets[i] = null;
					this.size--;
					return true;
				}
			}
		}
		
		return false;
	}

	public final synchronized boolean removeObject(final Object object)
	{
		if(object == null)
		{
			return false;
		}
		
		final Entry[] refBuckets;
		if((refBuckets = this.refHashTable[identityHashCode(object) & this.hashRange]) != null)
		{
			for(int i = 0; i < refBuckets.length; i++)
			{
				if(refBuckets[i] != null && refBuckets[i].get() == object)
				{
					synchRemoveEntry(this.oidHashTable[(int)refBuckets[i].objectId & this.hashRange], refBuckets[i]);
					refBuckets[i] = null;
					this.size--;
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public final synchronized <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		iterateEntries(this.oidHashTable, acceptor);
		return acceptor;
	}
	
	// Constants logic //
	
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
		
		final Entry[][] oidHashTable = this.oidHashTable;
		for(int h = 0; h < oidHashTable.length; h++)
		{
			final Entry[] bucket = oidHashTable[h];
			final Long bucketLength = countEntries(bucket);
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

	private HashStatisticsBucketBased synchCreateHashStatisticsRefs()
	{
		final EqHashTable<Long, Long> distributionTable = EqHashTable.New();

		final Entry[][] refHashTable = this.refHashTable;
		for(int h = 0; h < refHashTable.length; h++)
		{
			final Entry[] bucket = refHashTable[h];
			final Long bucketLength = countEntries(bucket);
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
	
	private static Long countEntries(final Entry[] bucket)
	{
		long count = 0;
		for(int i = bucket.length; --i >= 0;)
		{
			if(bucket[i] != null && !bucket[i].isEmpty())
			{
				count++;
			}
		}
		
		return count;
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

	private static final class Entry extends WeakReference<Object>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final long objectId;
		      int  refHash ;

		      
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Entry(final long objectId, final Object referent)
		{
			super(referent);
			this.objectId = objectId;
			this.refHash  = System.identityHashCode(referent);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		final boolean isEmpty()
		{
			return this.get() == null;
		}

	}

}
