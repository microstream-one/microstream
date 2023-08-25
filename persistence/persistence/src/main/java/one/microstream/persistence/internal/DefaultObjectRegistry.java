package one.microstream.persistence.internal;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.KeyValue;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.Set_long;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashStatisticsBucketBased;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyObject;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyObjectId;
import one.microstream.persistence.exceptions.PersistenceExceptionImproperObjectId;
import one.microstream.persistence.exceptions.PersistenceExceptionInvalidObjectRegistryCapacity;
import one.microstream.persistence.types.ObjectIdsProcessor;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.reference.Swizzling;
import one.microstream.typing.KeyValue;
import one.microstream.util.logging.Logging;

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
	
	private static Entry[] createHashTable(final int hashLength)
	{
		return new Entry[hashLength];
	}

	private static int calculateRequiredHashLength(final long minimumCapacity, final float hashDensity)
	{
		return XHashing.padHashLength((long)(minimumCapacity / hashDensity));
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
	 * @param hashDensity reasonable values are within [0.75; 2.00].
	 * @param minimumCapacity the initial minimum capacity
	 * @return the newly created {@link DefaultObjectRegistry}
	 */
	public static DefaultObjectRegistry New(
		final float hashDensity    ,
		final long  minimumCapacity
	)
	{
		return new DefaultObjectRegistry()
			.synchSetConfiguration(
				validateHashDensity(hashDensity),
				validateCapacity(minimumCapacity)
			)
			.synchReset()
		;
	}
	

	private final static Logger logger = Logging.getLogger(DefaultObjectRegistry.class);

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * Note:
	 * This does NOT replace locking the whole registry over a not to be "disrupted" process like loading
	 * (See BinaryLoader#get).
	 * This is just an internal lock to keep things consistent on a technical level.
	 * Locking the registry itself continuously across a whole process keeps things consistent
	 * on a business-logical level.
	 * Also note:
	 * The methode #processLiveObjectIds and #selectLiveObjectIds do not and MAY NOT lock the whole registry
	 * instance or it will create a deadlock with a loading process locking the registry and waiting for the
	 * load task to complete.
	 */
	private final Object mutex = new Object();
	
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

	private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

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

	/* note on naming:
	 * 
	 * All instance methods not starting with "synch~" must protected their logic
	 * by using a synchronized block synchronizing on the mutex instance.
	 * 
	 * AND
	 * 
	 * All instance methods starting with "synch~" must be called
	 * either
	 * 1.) inside a synchronized block
	 * OR
	 * 2.) by another method prefixed "synch~".
	 * 
	 * With this simple naming convention, it can be very easily checked if there
	 * are no loopholes in the concurrency handling architecture of this class.
	 */

	private int synchHashLength()
	{
		return this.hashRange + 1;
	}
	
	private void synchSetHashDensity(final float validHashDensity)
	{
		this.hashDensity = validHashDensity;
	}
	
	private void synchSetMinimumCapacity(final long validMinimumCapacity)
	{
		this.minCapacity = validMinimumCapacity;
	}
	
	final DefaultObjectRegistry synchSetConfiguration(
		final float hashDensity    ,
		final long  minimumCapacity
	)
	{
		this.synchSetHashDensity(hashDensity);
		this.synchSetMinimumCapacity(minimumCapacity);
		
		return this;
	}
	
	final DefaultObjectRegistry synchReset()
	{
		return this.synchReset(this.minCapacity);
	}
	
	final DefaultObjectRegistry synchReset(final long minimumCapacity)
	{
		this.size = 0;
		final int hashLength = calculateRequiredHashLength(minimumCapacity, this.hashDensity);
		this.synchSetHashTables(
			createHashTable(hashLength),
			createHashTable(hashLength)
		);
				
		return this;
	}
		
	private void synchSetHashTables(final Entry[] oidHashTable, final Entry[] refHashTable)
	{
		this.oidHashTable = oidHashTable;
		this.refHashTable = refHashTable;
		this.hashRange    = oidHashTable.length - 1;
		this.synchUpdateCapacity();
	}
	
	private void synchUpdateCapacity()
	{
		this.capacity = this.synchHashLength() >= XMath.highestPowerOf2_int()
			? Long.MAX_VALUE
			: (long)(this.synchHashLength() * this.hashDensity)
		;
	}
		
	@Override
	public final DefaultObjectRegistry Clone()
	{
		synchronized(this.mutex)
		{
			return DefaultObjectRegistry.New(this.hashDensity, this.minCapacity);
		}
	}

	@Override
	public final int hashRange()
	{
		synchronized(this.mutex)
		{
			return this.oidHashTable.length;
		}
	}

	@Override
	public final float hashDensity()
	{
		synchronized(this.mutex)
		{
			return this.hashDensity;
		}
	}
	
	@Override
	public final long minimumCapacity()
	{
		synchronized(this.mutex)
		{
			return this.minCapacity;
		}
	}

	@Override
	public final long capacity()
	{
		synchronized(this.mutex)
		{
			return this.capacity;
		}
	}

	@Override
	public final long size()
	{
		synchronized(this.mutex)
		{
			return this.size;
		}
	}

	@Override
	public final boolean isEmpty()
	{
		synchronized(this.mutex)
		{
			return this.size == 0;
		}
	}

	@Override
	public final boolean setHashDensity(final float hashDensity)
	{
		synchronized(this.mutex)
		{
			this.synchSetHashDensity(validateHashDensity(hashDensity));
			this.synchUpdateCapacity();
			return this.ensureCapacity(this.minCapacity);
		}
	}
	
	@Override
	public final boolean setConfiguration(
		final float hashDensity    ,
		final long  minimumCapacity
	)
	{
		synchronized(this.mutex)
		{
			// both values are checked before modifying any state
			validateHashDensity(hashDensity);
			validateCapacity(minimumCapacity);

			this.synchSetHashDensity(hashDensity);
			this.synchSetMinimumCapacity(minimumCapacity);

			this.synchUpdateCapacity();
			return this.ensureCapacity(minimumCapacity);
		}
	}
	
	@Override
	public final boolean setMinimumCapacity(final long minimumCapacity)
	{
		synchronized(this.mutex)
		{
			this.synchSetMinimumCapacity(validateCapacity(minimumCapacity));
			this.synchUpdateCapacity();
			return this.ensureCapacity(minimumCapacity);
		}
	}
	
	@Override
	public final boolean ensureCapacity(final long desiredCapacity)
	{
		synchronized(this.mutex)
		{
			/*
			 * Cannot use capacityHigh here, as this method is called after changing capacity-defining values.
			 * Instead, the actual hash length is checked to determine if the tables really are too small.
			 */
			validateCapacity(desiredCapacity);
			final int requiredHashLength = calculateRequiredHashLength(desiredCapacity, this.hashDensity);
			if(requiredHashLength > this.synchHashLength())
			{
				this.synchRebuild(requiredHashLength);
				return true;
			}

			return false;
		}
	}

	@Override
	public final boolean containsObjectId(final long objectId)
	{
		synchronized(this.mutex)
		{
			return this.synchContainsObjectId(objectId);
		}
	}

	private boolean synchContainsObjectId(final long objectId)
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
	public final long lookupObjectId(final Object object)
	{
		synchronized(this.mutex)
		{
			if(object == null)
			{
				throw new NullPointerException();
			}
			return this.synchLookupObjectId(object);
		}
	}
	
	private long synchLookupObjectId(final Object object)
	{
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
	public final Object lookupObject(final long objectId)
	{
		synchronized(this.mutex)
		{
			return this.synchLookupObject(objectId);
		}
	}
	
	private Object synchLookupObject(final long objectId)
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
	public final boolean isValid(final long objectId, final Object object)
	{
		synchronized(this.mutex)
		{
			// hacky flag, but no idea how to better prevent the code redundancy except with abstraction overkill.
			return this.synchInternalValidate(objectId, object, false);
		}
	}
	
	@Override
	public final void validate(final long objectId, final Object object)
	{
		synchronized(this.mutex)
		{
			// hacky flag, but no idea how to better prevent the code redundancy except with abstraction overkill.
			this.synchInternalValidate(objectId, object, true);
		}
	}
	
	private boolean synchInternalValidate(final long objectId, final Object object, final boolean throwException)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		
		final long registeredObjectId = this.synchLookupObjectId(object);
		if(registeredObjectId == objectId)
		{
			// already registered entry
			return true;
		}
		
		if(Swizzling.isNotFoundId(registeredObjectId))
		{
			final Object registeredObject = this.synchLookupObject(objectId);
			if(registeredObject == null)
			{
				// consistently not registered object
				return true;
			}
			
			if(!throwException)
			{
				return false;
			}
			if(registeredObject == object)
			{
				throw new PersistenceExceptionConsistency("Inconsistent object registry for objectId " + objectId);
			}
			throw new PersistenceExceptionConsistencyObject(objectId, registeredObject, object);
		}
		
		if(!throwException)
		{
			return false;
		}
		throw new PersistenceExceptionConsistencyObjectId(object, registeredObjectId, objectId);
	}
	
	@Override
	public final boolean registerObject(final long objectId, final Object object)
	{
		synchronized(this.mutex)
		{
			return this.synchRegisterObject(objectId, object);
		}
	}

	private boolean synchRegisterObject(final long objectId, final Object object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		if(Swizzling.isNotProperId(objectId))
		{
			throw new PersistenceExceptionImproperObjectId();
		}
		return this.synchAdd(objectId, object);
	}

	@Override
	public final Object optionalRegisterObject(final long objectId, final Object object)
	{
		synchronized(this.mutex)
		{
			if(object == null)
			{
				throw new NullPointerException();
			}
			if(Swizzling.isNotProperId(objectId))
			{
				throw new PersistenceExceptionImproperObjectId();
			}
			return this.synchAddGet(objectId, object);
		}
	}
	
	@Override
	public final boolean registerConstant(final long objectId, final Object constant)
	{
		synchronized(this.mutex)
		{
			if(!this.synchRegisterObject(objectId, constant))
			{
				return false;
			}
			this.synchEnsureConstantsHotRegistry().add(objectId, constant);

			return true;
		}
	}

	@Override
	public final <A extends PersistenceAcceptor> A iterateEntries(final A acceptor)
	{
		synchronized(this.mutex)
		{
			iterateEntries(this.oidHashTable, acceptor);
			return acceptor;
		}
	}
		
	private boolean synchAdd(final long objectId, final Object object)
	{
		if(this.synchAddCheck(objectId, object))
		{
			return false;
		}

		this.synchPutNewEntry(objectId, object);
		return true;
	}
		
	private void synchPutNewEntry(final long objectId, final Object object)
	{
		this.oidHashTable[(int)objectId & this.hashRange] =
		this.refHashTable[ hash(object) & this.hashRange] =
			new Entry(
				objectId,
				object,
				this.oidHashTable[(int)objectId & this.hashRange],
				this.refHashTable[ hash(object) & this.hashRange],
				this.queue
			)
		;

		if(++this.size > this.capacity)
		{
			this.synchIncreaseStorage();
		}
	}
	
	private boolean synchAddCheck(final long objectId, final Object object)
	{
		for(Entry e = this.oidHashTable[(int)objectId & this.hashRange]; e != null; e = e.oidNext)
		{
			if(e.objectId == objectId)
			{
				return this.synchHandleExisting(object, e);
			}
		}

		this.synchValidateObjectNotYetRegistered(objectId, object);
		return false;
	}
	
	private Object synchAddGetCheck(final long objectId, final Object object)
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
				this.synchRemoveEntry(e);
				break;
			}
		}

		// either no hash chain yet or no (live) entry for that objectId. Validate and signal need for registration.
		this.synchValidateObjectNotYetRegistered(objectId, object);

		return null;
	}
	
	private boolean synchHandleExisting(final Object object, final Entry entry)
	{
		if(entry.get() == object)
		{
			return true;
		}
		
		if(entry.get() != null)
		{
			throw new PersistenceExceptionConsistencyObject(entry.objectId, entry.get(), object);
		}

		this.synchValidateObjectNotYetRegistered(entry.objectId, object);
		this.synchRemoveEntry(entry);

		return false;
	}
	
	private void synchRemoveEntry(final Entry entry)
	{
		logger.debug("remove entry {}", entry.objectId);
		final boolean removeOid = removeFromOidTable(this.oidHashTable, (int)entry.objectId & this.hashRange, entry);
		final boolean removeRef = removeFromRefTable(this.refHashTable,      entry.refHash  & this.hashRange, entry);
		if(removeOid || removeRef)
		{
			this.size--;
		}
	}
	
	private static boolean removeFromOidTable(final Entry[] table, final int index, final Entry entry)
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
				return true;
			}
		}
		return false;
	}
	
	private static boolean removeFromRefTable(final Entry[] table, final int index, final Entry entry)
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
				return true;
			}
		}
		return false;
	}
		
	private void synchValidateObjectNotYetRegistered(final long objectId, final Object object)
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

	private Object synchAddGet(final long objectId, final Object object)
	{
		final Object alreadyRegistered;
		if((alreadyRegistered = this.synchAddGetCheck(objectId, object)) != null)
		{
			return alreadyRegistered;
		}

		this.synchPutNewEntry(objectId, object);
		return object;
	}
	
	// rebuilding and consolidation //
	
	@Override
	public final boolean consolidate()
	{
		synchronized(this.mutex)
		{
			return this.synchConsolidate();
		}
	}

	private boolean synchConsolidate()
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
		final int requiredHashLength = calculateRequiredHashLength(this.size, this.hashDensity);
		if(requiredHashLength != this.synchHashLength())
		{
			this.synchRebuild(requiredHashLength);
			return true;
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
		final Entry[] newOidHashTable = createHashTable(hashLength);
		final Entry[] newRefHashTable = createHashTable(hashLength);

		// orphaned entries are discarded and their total count is returned to be subtracted here.
		this.size -= rebuildTables(this.oidHashTable, newOidHashTable, newRefHashTable);

		// the new hash tables are set as the instance's storage structure.
		this.synchSetHashTables(newOidHashTable, newRefHashTable);

		/*
		 * Since rebuilding discards orphaned entries and reduces the size, it could be possible that
		 * a rebuild to increase the storage determines that it could actually shrink.
		 * The doubled performance cost in such cases should be well worth the automatic memory saving.
		 */
		this.checkForDecrease();

		// at some point, constant registration is completed, so an efficient storage form is preferable.
		this.synchEnsureConstantsColdStorage();
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
	public final void clear()
	{
		synchronized(this.mutex)
		{
			this.synchEnsureConstantsColdStorage();
			this.synchClear();
			this.synchReregisterConstants();
		}
	}
	
	@Override
	public final void clearAll()
	{
		synchronized(this.mutex)
		{
			this.synchClear();
		}
	}

	private void synchClear()
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
	public final void truncate()
	{
		synchronized(this.mutex)
		{
			// reinitialize storage strucuture with at least enough capacity for the incoming constants.
			this.synchEnsureConstantsColdStorage();
			this.synchReset(Math.max(this.constantsColdStorageObjects.length, this.minCapacity));
			this.synchReregisterConstants();
		}
	}
	
	@Override
	public final void truncateAll()
	{
		synchronized(this.mutex)
		{
			// hash table reset, no constants reregistering.
			this.synchReset();
		}
	}
	
	// Constants handling //
	
	private void synchReregisterConstants()
	{
		synchronized(this.mutex)
		{
			final Object[] constantsObjects = this.constantsColdStorageObjects;
			final long[] constantsObjectIds = this.constantsColdStorageObjectIds;

			for(int i = 0; i < constantsObjects.length; i++)
			{
				// NOT registerConstant() at this point!
				this.synchRegisterObject(constantsObjectIds[i], constantsObjects[i]);
			}
		}
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
	
	@Override
	public boolean processLiveObjectIds(final ObjectIdsProcessor processor)
	{
		synchronized(this.mutex)
		{
			processor.processObjectIdsByFilter(this::synchIsLiveObjectId);
			return true;
		}
	}

	final boolean synchIsLiveObjectId(final long objectId)
	{
		final boolean result = this.synchContainsObjectId(objectId);
		
		logger.debug("ObjectRegistry checking OID " + objectId + ": " + result);

		return result;
	}

	@Override
	public Set_long selectLiveObjectIds(final Set_long objectIdsBaseSet)
	{
		synchronized(this.mutex)
		{
			return objectIdsBaseSet.filter(this::synchIsLiveObjectId);
		}
	}
	
	// HashStatistics //
	
	@Override
	public final XGettingTable<String, HashStatisticsBucketBased> createHashStatistics()
	{
		synchronized(this.mutex)
		{
			return EqHashTable.New(
				KeyValue("PerObjectIds", this.synchCreateHashStatisticsOids()),
				KeyValue("PerObjects"  , this.synchCreateHashStatisticsRefs())
			);
		}
	}
	
	private HashStatisticsBucketBased synchCreateHashStatisticsOids()
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

	private HashStatisticsBucketBased synchCreateHashStatisticsRefs()
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

	@Override
	public void cleanUp()
	{
		synchronized (this.mutex)
		{
			long counter = 0;
			
			for (Reference<? extends Object> e; (e = this.queue.poll()) != null; )
			{
				//System.out.println(e);
				this.synchRemoveEntry((Entry)e);
				counter++;
			}
			
			logger.info("Cleaned {} gc entries", counter);
			
			this.checkForDecrease();
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// member types //
	/////////////////

	static final class Entry extends WeakReference<Object>
	{
		final long objectId;
		      int  refHash ;
		      Entry oidNext, refNext;
		
		Entry(final long objectId, final Object referent, final Entry oidNext, final Entry refnext, final ReferenceQueue<Object> queue)
		{
			super(referent, queue);
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
