package net.jadoth.persistence.internal;

import static java.lang.System.identityHashCode;
import static net.jadoth.X.notNull;

import java.lang.ref.WeakReference;

import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectRegistry;

public final class BetterObjectRegistry
{
	public static final int minimumBucketLength()
	{
		// below that, the overhead for the quad-bucket-arrays does not pay off and performance gain wouldn't be much.
		return 8;
	}
	
	public static final float defaultHashDensity()
	{
		// start low
		return minimumBucketLength();
	}
	
	public static final int minimumHashRange()
	{
		// Object registries are meant to keep thousands of objects, even in small applications.
		return (1<<10) - 1;
	}
	
	public static final float defaultHashRange()
	{
		// start low
		return minimumHashRange();
	}
	
	public static final int maximumHashRange()
	{
		// technical limitation of Java arrays
		return Integer.MAX_VALUE;
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
	 * Due to hashing arithmetic, a hash range is always in the form of 2^n - 1, or in shift-writing: (1<<n) - 1.
	 */
	private int hashRange;
	
	/**
	 * The initial length of a hashing bucket array. Correlates to {@link #hashDensity}.
	 * Also serves as the increment in case a bucket becomes too small.
	 */
	private int bucketStartLength;
	/* (26.11.2018 TM)FIXME: bucket length must either be an exact fit or lookups have to null-check
	 * exact fit would mean a lot of copying overhead, but would have a lot of advantages, too:
	 * - no locking on buckets during lookup required since all modifications create new bucket instances
	 * - no memory overhead for partially filled buckets
	 * - lookups do not require null-checks
	 */
	
	/**
	 * The amount of conainable entries before a rebuild is required
	 */
	private long capacity;
	
	/**
	 * The total entry count (including existnig orphan entries)
	 */
	private long size;
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public long lookupObjectId(final Object object)
	{
		notNull(object);
		
		// the global lock is held as short as possible while still guaranteeing consistency.
		final Item[] refHashedRefKeys;
		final long[] refHashedOidVals;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			refHashedRefKeys = this.refHashedRefKeysTable[identityHashCode(object) & this.hashRange];
			refHashedOidVals = this.refHashedOidValsTable[identityHashCode(object) & this.hashRange];
		}
		
		// (26.11.2018 TM)NOTE: see "local locking required?" in #lookupObject
		
		for(int i = 0; i < refHashedRefKeys.length; i++)
		{
			// can be null for orphan entries
			if(refHashedRefKeys[i].get() == object)
			{
				return refHashedOidVals[i];
			}
		}
		
		return Persistence.nullId();
	}
	
	public final Object lookupObject(final long objectId)
	{
		// the global lock is held as short as possible while still guaranteeing consistency.
		final long[] oidHashedOidKeys;
		final Item[] oidHashedRefVals;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			oidHashedOidKeys = this.oidHashedOidKeysTable[(int)objectId & this.hashRange];
			oidHashedRefVals = this.oidHashedRefValsTable[(int)objectId & this.hashRange];
		}
		
		/* (26.11.2018 TM)TODO: local locking required?
		 * The key bucket instances could be used as bucket-local locking subjects,
		 * but I am honestly not sure down to the last detail if that is necessary at all.
		 * Aren't the bucket arrays at this point thread-local copies on the stack anyway?
		 * Not getting a state mutation - or preventing one before the read is done - would
		 * be the same thing that a thread local stack-located copy is, anyway: it has
		 * the state at the time of the lock-protected query. A simple happens-before relation
		 * regarding any mutations that occur afterwards.
		 * Or is this wrong? Can the retrieved bucket array here be concurrency changed by another
		 * thread despite being purely stack-based from here on? Maybe caused by an OS interrupt or whatever?
		 * If there turn out to be concurrency issues with this implementation, these details should be clarified
		 */
		
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				return oidHashedRefVals[i].get();
			}
		}
		
		return null;
	}
	
	public boolean containsObjectId(final long objectId)
	{
		// the global lock is held as short as possible while still guaranteeing consistency.
		final long[] oidHashedOidKeys;
		synchronized(this)
		{
			// access to all global mutable state is protected by a global lock
			oidHashedOidKeys = this.oidHashedOidKeysTable[(int)objectId & this.hashRange];
		}
		
		// (26.11.2018 TM)NOTE: see "local locking required?" in #lookupObject
		
		for(int i = 0; i < oidHashedOidKeys.length; i++)
		{
			if(oidHashedOidKeys[i] == objectId)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public synchronized <A extends PersistenceObjectRegistry.Acceptor> A iterateEntries(final A acceptor)
	{
		// iterating everything has so many accesses that a total lock is the only consistent thing to do.
		
		final long[][] oidHashedOidKeysTable = this.oidHashedOidKeysTable;
		final Item[][] oidHashedRefValsTable = this.oidHashedRefValsTable;
		
		final int oidHashedOidKeysLength = oidHashedOidKeysTable.length;
		
		for(int t = 0; t < oidHashedOidKeysLength; t++)
		{
			final long[] oidHashedOidKeys = oidHashedOidKeysTable[t];
			final Item[] oidHashedRefVals = oidHashedRefValsTable[t];
			
			for(int i = 0; i < oidHashedOidKeys.length; i++)
			{
				final Object instance = oidHashedRefVals[i].get();
				if(instance != null)
				{
					acceptor.accept(oidHashedOidKeys[i], instance);
				}
				
			}
		}
		
		return acceptor;
	}
	
	

	public boolean registerObject(final long objectId, final Object object)
	{
		
	}
	
	
	
	
	public interface Item
	{
		public Object get();
	}
	
	
	static final class ObjectEntry extends WeakReference<Object> implements Item
	{
		ObjectEntry(final Object referent)
		{
			super(referent);
		}
	}
	
	static final class ConstantEntry implements Item
	{
		private final Object referent;
	
		ConstantEntry(final Object referent)
		{
			super();
			this.referent = referent;
		}
		
		@Override
		public final Object get()
		{
			return this.referent;
		}
	}
	
}
