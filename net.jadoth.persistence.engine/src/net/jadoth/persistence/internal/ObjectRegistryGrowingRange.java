package net.jadoth.persistence.internal;

import static java.lang.System.identityHashCode;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import net.jadoth.exceptions.NumberRangeException;
import net.jadoth.math.XMath;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObject;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import net.jadoth.persistence.exceptions.PersistenceExceptionNullObjectId;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectRegistry;

public final class ObjectRegistryGrowingRange implements PersistenceObjectRegistry
{
	/* Notes:
	 * - funny find: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451 . welcome to this user code class!
	 * - all methods prefixed with "synch" are only called from inside a synchronized context
	 */

	/* Memory demands per entry:
	 * A) 64 bit coops
	 *  08 2*4 byte in hashing arrays
	 *  08 2*4 byte in buckets arrays
	 *  08 entry instance object header
	 *  04 int hash
	 *  08 long oid
	 *  08 long tid
	 *  04 coop ref (to WeakReference instance)
	 *  04 alignment overhead
	 *  32 WeakReference instance (08 + 4*coop + alignment overhead 4)
	 * ---------------------------------
	 *  80 total size
	 *
	 * B) 64 bit full oops
	 *  16 2*8 byte in hashing arrays
	 *  16 2*8 byte in buckets arrays
	 *  16 entry instance object header
	 *  08 long oid
	 *  08 long tid
	 *  04 int hash
	 *  04 primitive alignment overhead
	 *  08 oop ref (to WeakReference instance)
	 *  48 WeakReference instance (16 + 4*oop + no alignment overhead)
	 * ---------------------------------
	 * 128 total size
	 *
	 * Note: in both modes, there are 4 bytes to spare per entry. Maybe useful for future flags etc.
	 */

	/* maybe future to-do: SwizzleRegistryFixedRange
	 * Once stable, derive a SwizzleRegistryFixedRange implementation from this class.
	 * Hash range (slots length) is set once in the constructor and is then unchangeable (final arrays).
	 * Advantage: avoids unnecessary increase checks, probably easier (= faster) to get it thread safe.
	 */




	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/**
	 * Default or minimal size for the registry's hash range (hash table array size).
	 * This is intentionally pretty high, because it has to hold a high amount of references
	 * even without a big data model. It has to hold the following references:
	 * <ul>
	 * <li>More than 1000 cached java primitive wrapper instances</li>
	 * <li>All swizzling-relevant types (including abstract classes and interfaces)</li>
	 * <li>All swizzling-relevant constants</li>
	 * <li>All actual data model (entity) instances to be handled</li>
	 * </ul>
	 * Additionally, as the performance of a general "contain everything" registry like this is more important
	 * than small memory footprint, the hash range is better chosen generously high to have even a large
	 * number if entries spread out well.
	 */
	private static final int   MINIMUM_SLOT_LENGTH  = 1024; // 1 << 10
	private static final float DEFAULT_HASH_DENSITY = 1.0f;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int padCapacity(final int desiredSlotLength)
	{
		if(XMath.isGreaterThanOrEqualHighestPowerOf2Integer(desiredSlotLength))
		{
			// (16.04.2016)TODO: why isn't this max integer? See general purpose hash collections
			return XMath.highestPowerOf2Integer();
		}
		int slotCount = MINIMUM_SLOT_LENGTH;
		while(slotCount < desiredSlotLength)
		{
			slotCount <<= 1;
		}
		return slotCount;
	}

	private static float positive(final float value) throws NumberRangeException
	{
		if(value > 0f)
		{
			return value;
		}
		throw new NumberRangeException();
	}
	


	public static ObjectRegistryGrowingRange New()
	{
		return New(MINIMUM_SLOT_LENGTH);
	}

	public static ObjectRegistryGrowingRange New(final int slotSize)
	{
		return New(slotSize, DEFAULT_HASH_DENSITY);
	}

	public static ObjectRegistryGrowingRange New(final float hashDensity)
	{
		return New(MINIMUM_SLOT_LENGTH, hashDensity);
	}

	public static ObjectRegistryGrowingRange New(
		final int   slotSize   ,
		final float hashDensity
	)
	{
		return new ObjectRegistryGrowingRange(
			padCapacity(slotSize),
			positive(hashDensity)
		);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/* (31.10.2018 TM)TODO: SwizzleRegistry improvment
	 * Maybe split the Entry[][] into a long[][] just for the oid and a corresponding 2D-array
	 * of Entry (extrends WeakReference).
	 * This would probably make the lookup per OID ("loading") faster due to less pointer chasing.
	 * But maybe not due to passing around references to two arrays instead of one.
	 */
	
	private Entry[][] slotsPerOid; // "primary" slots. See put() and increaseStorage() methods
	private Entry[][] slotsPerRef;
	private int       size       ;
	private float     hashDensity;
	private int       capacity   ;
	private int       modulo     ; // shortcut for "slots.length - 1" (yields around 3% performance in put tests)

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ObjectRegistryGrowingRange(final int paddedSlotSize, final float hashDensity)
	{
		super();
		this.slotsPerOid = new Entry[paddedSlotSize][];
		this.slotsPerRef = new Entry[paddedSlotSize][];
		this.capacity    = (int)(paddedSlotSize * (this.hashDensity = hashDensity));
		this.size        = 0;
		this.modulo      = paddedSlotSize - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetSlotsPerRef()
	{
		return this.slotsPerRef;
	}

	// this method is a synchronization "point" for the otherwise thread-local lookup algorithm
	private synchronized Entry[][] synchronizedGetSlotsPerOid()
	{
		return this.slotsPerOid;
	}

	private synchronized boolean synchronizedPut(final long oid, final Object ref)
	{
		if(alreadyRegisteredReference(this.slotsPerRef[identityHashCode(ref) & this.modulo], oid, ref))
		{
			return false;
		}

		/* at this point, the reference is definitely not contained in the registry.
		 * Either because it never has been or because there's an orphan weak entry with its oid.
		 * Check for colliding id and orphan case
		 */
		final Entry entry;
		if((entry = synchLookupEntryPerOid(this.slotsPerOid[(int)oid & this.modulo], oid)) != null)
		{
			synchRehash(this.slotsPerRef, entry.set(ref), entry);
			return false;
		}

		/* at this point, neither the reference nor a fitting orphan entry is contained in the registry
		 * and there is no colliding oid, either.
		 * So create and put a new Entry for it
		 */
		this.synchPutNewEntry(new Entry(oid, ref));
		return true;
	}

	private synchronized Object synchronizedAddRef(final long oid, final Object ref) // dirty flag, bua
	{
		if(alreadyRegisteredReference(this.slotsPerRef[identityHashCode(ref) & this.modulo], oid, ref))
		{
			return ref;
		}

		/* at this point, the reference is definitely not contained in the registry.
		 * either because it never has been or because there's an orphan weak entry with its id.
		 * Check for colliding id and orphan case
		 */
		final Entry entry;
		if((entry = synchLookupEntryPerOid(this.slotsPerOid[(int)oid & this.modulo], oid)) != null)
		{
			final Object current;
			if((current = entry.ref.get()) != null)
			{
				return current; // no matter if current is already the same reference or not, just return it (add logic)
			}
			synchRehash(this.slotsPerRef, entry.set(ref), entry);
			return ref;
		}

		/* at this point, neither the reference nor an orphan entry is not contained in the registry
		 * and there is no colliding id either.
		 * So create a new Entry for it
		 */
		this.synchPutNewEntry(new Entry(oid, ref));
		return ref;
	}

	private void synchPutNewEntry(final Entry entry)
	{
		// at this point, the new entry can and will definitely be added. So increase size here and if necessary slots.
		if(++this.size >= this.capacity)
		{
			this.synchIncreaseStorage();
		}

		// storage is defintely big enough, size has already been incremented, entry can be simply inserted
		synchInsertEntry(this.slotsPerOid, this.slotsPerRef, entry);
	}

	private void synchIncreaseStorage()
	{
		/* Notes:
		 * - even here, orphan entries are kept because they still represent an oid->tid mapping
		 * - as a consequence, size can not change here and hence is not recalculated and not re-set.
		 * - slotsPerOid potentially contains more entries than slotsPerRef because of empty oid->tid entries
		 * - it is mathematically impossible that rebuilding would require another increase
		 * - recalculating modulo every time when inserting is inefficent here, but more efficient for single puts.
		 */
		if(XMath.isGreaterThanOrEqualHighestPowerOf2Integer(this.slotsPerOid.length))
		{
			this.capacity = Integer.MAX_VALUE; // disable special case after running into it once (cool 8-) )
			return;
		}
		this.synchRebuild(this.slotsPerOid.length << 1);
	}
	
	
	/**
	 * Rebuild with maintained hash size to clean up orphan entries.
	 * 
	 */
	private void synchRebuild()
	{
		this.synchRebuild(this.slotsPerOid.length);
	}

	private void synchRebuild(final int slotLength)
	{
//		XDebug.debugln("rebuilding to length " + slotLength);
		final Entry[][] newSlotsPerOid = new Entry[slotLength][];
		final Entry[][] newSlotsPerRef = new Entry[slotLength][];

		synchRebuildSlots(this.slotsPerOid, newSlotsPerOid, newSlotsPerRef);

		this.capacity    = (int)(slotLength * this.hashDensity);
		this.slotsPerOid = newSlotsPerOid;
		this.slotsPerRef = newSlotsPerRef;
		this.modulo      = slotLength - 1;
//		XDebug.debugln(" * done. new capacity = " + this.capacity);
	}



	private static long lookupOid(final Entry[] bucketsR, final Object object)
	{
		/* Notes:
		 * - if thread cache is up to date, the read-only algorithm on the slots array is thread-safe and correct
		 * - orphan buckets are intentionally not checked and not removed on read-only lookups
		 * - array length is intentionally not cached due to normally short array length and maybe JIT optimization
		 * - recalculating the modulo here allows the method to be static and skip subject passing
		 * - even during concurrent put, rebuild, clean, this algorithm is thread safe
		 */
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].ref.get() == object)
				{
					return bucketsR[i].oid;
				}
			}
		}
		
		return Persistence.nullId();
	}

	private static Object lookupObject(final Entry[] bucketsI, final long oid)
	{
		/* Notes:
		 * - if thread cache is up to date, the read-only algorithm on the slots array is thread-safe and correct
		 * - array length is intentionally not cached due to normally short array length and maybe JIT optimization
		 * - even during concurrent put, rebuild, clean, this algorithm is thread safe
		 * - oids are assumed to be roughly sequential, hence the lower 32 bit are sufficient for proper distribution
		 */
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					return bucketsI[i].ref.get();
				}
			}
		}
		
		/* (31.10.2018 TM)TODO: Decoupled constant registry
		 * a lookup in a decoupled constant registry could be performed here.
		 * This would cause the following changes:
		 * - none to a proper OID lookup (the vast majority of lookups)
		 * - a tiny delay (double lookup) to looking up constants, which is not noticable in the grand scheme
		 * - a tiny delay (double lookup) to looking up erroneously unresolvable OIDs, which is irrelevant.
		 * - a tiny delay (double lookup) to looking up OIDs during loading. And that is the painful point.
		 * - yield a modular and immutable constant-registry that could be shared (e.g. OGC) and spared from clearing.
		 * Also see issue JET-48.
		 * 
		 * The only problem is that this method has a static context and cannot relay to a constant registry.
		 * Making it an instance method might cost performance (has to be tested)
		 * Or wait a second:
		 * The constant registry only handles constants (JSL cached instances and references to constant field instances)
		 * So it is by definition static, anyway.
		 * The only problem is that the RootResolver is dynamic that is executed long after static initializers.
		 * Maybe the constant registry has to be pseudo-immutable then, with the root resolving being allowed
		 * to add resolved instances.
		 * However, that creates new problems:
		 * - What if there is more than one RootResolver in the same process?
		 * - If that registry is shared with an OGC channel, it might even be a security loophole
		 * (know the ID of an important constant and you can request everything it references)
		 */
		
		return null;
	}

	private static boolean containsOid(final Entry[] bucketsI, final long oid)
	{
		/* Notes:
		 * - if thread cache is up to date, the read-only algorithm on the slots array is thread-safe and correct
		 * - array length is intentionally not cached due to normally short array length and maybe JIT optimization
		 * - even during concurrent put, rebuild, clean, this algorithm is thread safe
		 * - oids are assumed to be roughly sequential, hence the lower 32 bit are sufficient for proper distribution
		 */
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					return true;
				}
			}
		}
		return false;
	}


	private static void validateEntryOid(final Entry entry, final long oid, final Object ref)
	{
		if(entry.oid != oid)
		{
			throw new PersistenceExceptionConsistencyObjectId(ref, entry.oid, oid);
		}
	}

	private static boolean alreadyRegisteredReference(
		final Entry[] bucketsR,
		final long    oid     ,
		final Object  ref
	)
	{
		if(bucketsR != null)
		{
			for(int i = 0; i < bucketsR.length; i++)
			{
				if(bucketsR[i] != null && bucketsR[i].ref.get() == ref)
				{
					validateEntryOid(bucketsR[i], oid, ref);
					return true;
				}
			}
		}
		return false;
	}

	private static Entry synchLookupEntryPerOid(final Entry[] bucketsI, final long oid)
	{
		if(bucketsI != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == oid)
				{
					return bucketsI[i];
				}
			}
		}
		return null;
	}

	private static Entry[] synchEnlargeBuckets(final Entry[] oldBuckets, final Entry entry)
	{
		/* Notes:
		 * - orphan entries are kept intentionally
		 * - bucket length is very unlikely to grow beyond max pow2 int, so no grow length check is performed
		 */
		final Entry[] newBuckets;
		System.arraycopy(oldBuckets, 0, newBuckets = new Entry[oldBuckets.length << 1], 0, oldBuckets.length);
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

	private static void synchRehash(final Entry[][] slotsPerRef, final int oldHash, final Entry entry)
	{
		if(oldHash >= 0)
		{
			if(oldHash > 0)
			{
				synchRemoveEntry(slotsPerRef[oldHash & slotsPerRef.length - 1], entry);
			}
			synchPutEntry(slotsPerRef, entry.hash & slotsPerRef.length - 1, entry);
		}
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

	private static void synchInsertEntry(final Entry[][] slotsPerOid, final Entry[][] slotsPerRef, final Entry entry)
	{
		synchPutEntry(slotsPerOid, (int)entry.oid & slotsPerOid.length - 1, entry);
		if(!entry.isEmpty())
		{
			// putting in a ref bucket makes no sense for neither hollow nor orphan (= empty) entries.
			synchPutEntry(slotsPerRef, entry.hash & slotsPerOid.length - 1, entry);
		}
	}

	private static Entry[] synchCreateNewBuckets(final Entry entry)
	{
		/* starting at only one bucket is a little inefficient for enlargement but pretty memory-efficient.
		 * As the goal is to produce as few collisions as possible, single-entry buckets is the common case.
		 */
		return new Entry[]{entry};
	}

	private static void synchPutEntry(final Entry[][] slots, final int idx, final Entry entry)
	{
		if(slots[idx] == null)
		{
			// case 1: slot still empty. Create new buckets array.
			slots[idx] = synchCreateNewBuckets(entry);
		}
		else if(!synchUseEmptyBucket(slots[idx], entry)) // case 2: buckets array still has an empty bucket.
		{
			// case 3: buckets array full. Enlarge and insert.
			slots[idx] = synchEnlargeBuckets(slots[idx], entry);
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
			if(buckets[i] != null)
			{
				synchInsertEntry(newSlotsPerOid, newSlotsPerRef, buckets[i]);
			}
		}
	}

	private static void iterateEntries(final Entry[][] slots, final PersistenceObjectRegistry.Acceptor acceptor)
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
						acceptor.accept(buckets[b].id(), buckets[b].reference());
					}
				}
			}
		}
	}

	private static void synchClearOrphanEntries(final Entry[][] slots)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null && buckets[b].isOrphan())
					{
						buckets[b] = null;
					}
				}
			}
		}
	}
	
	private static void synchClearEntries(final Entry[][] slots, final Predicate<? super PersistenceObjectRegistry.Entry> filter)
	{
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				final Entry[] buckets = slots[s];
				for(int b = 0; b < buckets.length; b++)
				{
					if(buckets[b] != null && (buckets[b].isOrphan() || filter.test(buckets[b])))
					{
						buckets[b] = null;
					}
				}
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public long size()
	{
		return this.size;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public int hashRange()
	{
		return this.slotsPerOid.length;
	}

	@Override
	public float hashDensity()
	{
		return this.hashDensity;
	}

	@Override
	public long capacity()
	{
		return this.capacity;
	}

	@Override
	public synchronized void clear()
	{
		final Entry[][] slotsI = this.slotsPerOid, slotsR = this.slotsPerRef;
		for(int i = 0; i < slotsI.length; i++)
		{
			slotsI[i] = slotsR[i] = null;
		}
		this.size = 0;
	}

	@Override
	public boolean containsObjectId(final long oid)
	{
		return containsOid(this.synchronizedGetSlotsPerOid()[(int)oid & this.modulo], oid);
	}

	@Override
	public long lookupObjectId(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		return lookupOid(this.synchronizedGetSlotsPerRef()[identityHashCode(object) & this.modulo], object);
	}

	@Override
	public Object lookupObject(final long oid)
	{
		return lookupObject(this.synchronizedGetSlotsPerOid()[(int)oid & this.modulo], oid);
	}
	
//	@Override
//	public Object registerObjectId(final long oid)
//	{
//		if(oid == Persistence.nullId())
//		{
//			throw new PersistenceExceptionNullObjectId();
//		}
//		return this.synchronizedPutId(oid);
//	}

	@Override
	public boolean registerObject(final long oid, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(oid == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}

		return this.synchronizedPut(oid, object);
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
		return this.synchronizedAddRef(oid, object);
	}

	@Override
	public synchronized boolean removeObjectById(final long id)
	{
		if(id == Persistence.nullId())
		{
			throw new PersistenceExceptionNullObjectId();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerOid[(int)id & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].oid == id)
				{
					synchRemoveEntry(this.slotsPerRef[bucketsI[i].hash & this.modulo], bucketsI[i]);
					bucketsI[i] = null;
					this.size--;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public synchronized boolean removeObject(final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		final Entry[] bucketsI;
		if((bucketsI = this.slotsPerRef[identityHashCode(object) & this.modulo]) != null)
		{
			for(int i = 0; i < bucketsI.length; i++)
			{
				if(bucketsI[i] != null && bucketsI[i].ref.get() == object)
				{
					synchRemoveEntry(this.slotsPerOid[(int)bucketsI[i].oid & this.modulo], bucketsI[i]);
					bucketsI[i] = null;
					this.size--;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public <A extends PersistenceObjectRegistry.Acceptor> A iterateEntries(final A acceptor)
	{
		iterateEntries(this.synchronizedGetSlotsPerOid(), acceptor);
		return acceptor;
	}

	@Override
	public synchronized void shrink()
	{
		final int requiredSlotLength;
		if((requiredSlotLength = padCapacity((int)(this.size / this.hashDensity))) >= this.slotsPerOid.length)
		{
			return; // can't shrink, abort
		}
		this.synchRebuild(requiredSlotLength);
	}

	@Override
	public synchronized void cleanUp()
	{
		this.clearOrphanEntries();
		this.synchRebuild();
	}

	@Override
	public synchronized void clearOrphanEntries()
	{
		synchClearOrphanEntries(this.slotsPerOid);
		synchClearOrphanEntries(this.slotsPerRef);
	}
	
	@Override
	public synchronized void clear(final Predicate<? super PersistenceObjectRegistry.Entry> filter)
	{
		synchClearEntries(this.slotsPerOid, filter);
		synchClearEntries(this.slotsPerRef, filter);
		this.synchRebuild(this.slotsPerOid.length);
	}



	///////////////////////////////////////////////////////////////////////////
	// member types     //
	/////////////////////

	/* (16.03.2015 TM)TODO: let SwizzleRegistryGrowingRange$Entry extend WeakReference
	 * This saves memory footprint.
	 * Must however on referen update replace Entries with new instances instead of just setting a new WeakReference.
	 */
	private static final class Entry implements PersistenceObjectRegistry.Entry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final long                  oid ;
		      int                   hash; // causes 4 byte alignment overhead with and without coops. => 4 byte to spare
		      WeakReference<Object> ref ;

		      
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Entry(final long oid, final Object ref)
		{
			super();
			this.oid  = oid;
			this.ref  = new WeakReference<>(ref);
			this.hash = identityHashCode(ref);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long id()
		{
			return this.oid;
		}

		@Override
		public final Object reference()
		{
			return this.ref.get();
		}

		/**
		 * @return {@code true} if this entry has lost its referent.
		 *
		 * @see #isEmpty()
		 */
		final boolean isOrphan()
		{
			return this.ref.get() == null && this.hash > 0;
		}

		/**
		 * @return {@code true} if this entry's referent is {@code null} for any reason, {@code false} otherwise.
		 *
		 * @see #isOrphan()
		 */
		final boolean isEmpty()
		{
			return this.ref.get() == null;
		}

		final int set(final Object ref)
		{
			// case: hollow oid<->tid entry
			if(this.hash == 0)
			{
				this.ref = new WeakReference<>(ref);
				this.hash = System.identityHashCode(ref);
				return 0;
			}

			// case: already contains this reference
			if(this.ref.get() == ref)
			{
				return -1;
			}

			// case: orphan entry with same oid
			if(this.ref.get() == null)
			{
				this.ref = new WeakReference<>(ref);
				// hardly unlikely that a new instance will have the same hashcode, so ignore check
				final int oldHash = this.hash; // required to rehash entry in the ref slots
				this.hash = identityHashCode(ref);
				return oldHash;
			}
			throw new PersistenceExceptionConsistencyObject(this.oid, this.ref.get(), ref);
		}

	}

	@Deprecated
	public void DEBUG_analyze()
	{
		System.out.println("size        = " + this.size);
		System.out.println("hashDensity = " + this.hashDensity);
		System.out.println("hashRange   = " + this.slotsPerOid.length);
		System.out.println("capacity    = " + this.capacity);
		DEBUG_printAnalysis("perRef", DEBUG_analyze(this.slotsPerRef));
		DEBUG_printAnalysis("perOid", DEBUG_analyze(this.slotsPerOid));

	}

	@Deprecated
	private static void DEBUG_printAnalysis(final String label, final int[] analysis)
	{
		System.out.println(label + ":");
		System.out.println("empty\t" + analysis[0]);
		for(int i = 1; i < analysis.length; i++)
		{
			System.out.println(XMath.pow(2, i - 1) + "\t" + analysis[i]);
		}
		System.out.println("----");
	}

	// CHECKSTYLE:OFF only for debugging
	@Deprecated
	private static int[] DEBUG_analyze(final Entry[][] slots)
	{
		final int[] bucketsPow2Lenghts = new int[30];
		for(int s = 0; s < slots.length; s++)
		{
			if(slots[s] != null)
			{
				bucketsPow2Lenghts[XMath.log2pow2(slots[s].length) + 1]++;
			}
			else
			{
				bucketsPow2Lenghts[0]++;
			}
		}
		return bucketsPow2Lenghts;
	}
	// CHECKSTYLE:ON



//	public static void main(final String[] args)
//	{
//		// -XX:-UseCompressedOops
//		System.out.println(
//			2*2*Memory.byteSizeReference()
//			+ Memory.byteSizeInstance(Entry.class)
//			+ Memory.byteSizeInstance(WeakReference.class)
//		);
//	}

}
