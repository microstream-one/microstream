package net.jadoth.locking;

import java.util.function.Consumer;

import net.jadoth.collections.HashTable;
import net.jadoth.functional.BiProcedure;
import net.jadoth.util.KeyValue;


/* (12.11.2015 TM)TODO: LockManager concept
 * - locklevels to properly handle nested calling contexts
 *
 * - read&readwrite (shared and exclusive) locking? (querying for canRead() and hasReadOnlyLock())
 *
 * - lots of convenience methods like tryExecute(), awaitExecute(), etc.
 * - await functionality in an extra type referencing the actual "primitive" lockmanager
 *   Hierarchy:
 *   > LockManager
 *   > WaitingLockManager extends LockManager
 *   > EnqueuingLockManager extends WaitingLockManager
 *   > PrioritizingLockManager extends EnqueuingLockManager
 *
 * - interfaces and factory/foundation type
 * - querying, iteration, etc.
 * - querying trial methods and "normal" trial methods (returning true/false)
 * - modularization (e.g. is it an error to try to release a lock with no owner? or "failure handler" in general)?
 * - per-owner querying (on the fly collecting, no permanent state extension)
 * - per-owner co-table? (redundant state?)
 * - timeouts?
 * - overrides / administrative privileges?
 * - callbacks? (or done via interfaces?)
 * - queueing?
 * - deadlock-checking?
 * - persistent lock manager (remember locks even after restart/crash?)
 * - hierarchical simplification:
 *   > instance-based LockManager(s)
 *   > global default LockManager
 *   > Simple "Lock" Instances knowing the global LockManager and registering themselves
 *   > One global default Lock instance for simplistic carefree unscalable "lock everthing" approach.
 *
 * - custom internalized hashTable implemententation with .wait() and .notify() on the (private) entry instances
 */
public interface LockManager<O> extends LockOwnerTypeHolder<O>
{
	public O queryLockOwner(Object object);

	public boolean ownsLock(O owner, Object object);

	public boolean ownsLocksAll(O owner, Object... objects);

	public O tryLock(O owner, Object object);

	/**
	 * Acquire the locks on ALL passed objects if possible or on none at all.
	 *
	 * @param owner
	 * @param objects
	 * @return the amount of locks that could not have been acquired (meaning 0 indicates success).
	 */
	public default int tryLockAllOrNone(final O owner, final Object... objects)
	{
		return this.reportingTryLockAllOrNone(owner, objects, null);
	}

	public default int tryLockEager(final O owner, final Object... objects)
	{
		return this.reportingTryLockEager(owner, objects, null);
	}

	public int reportingTryLockAllOrNone(O owner, Object[] objects, O[] owners);

	public int reportingTryLockEager(O owner, Object[] objects, O[] owners);

	public O releaseLock(O owner, Object object);

	/**
	 * Ensures that none of the passed objects are locked for the passed owner.
	 * Does not give any guarantee that the passed objects are not locked by another owner.
	 * As a hint to existing other owner, the number of actually released locks is returned.
	 *
	 * @param owner
	 * @param objects
	 * @return the number of actually released locks.
	 */
	public int releaseLocksAll(O owner, Object... objects);

	/**
	 * acquires the lock for {@code next}, then executes {@code logic}, then releases the owned lock for {@code current}
	 *
	 * @param owner
	 * @param current
	 * @param next
	 * @param logic
	 * @return
	 */
	public <T1, T2> O transferLock(O owner, T1 current, T2 next, BiProcedure<? super T1, ? super T2> logic);

	public default <T1, T2> O transferLock(final O owner, final T1 current, final T2 next)
	{
		return this.transferLock(owner, current, next, null);
	}

	public <T> boolean executeLocked(O owner, T object, Consumer<? super T> logic);

	public <T> boolean executeLocked(
		O                                      owner       ,
		T                                      object      ,
		Consumer<? super T>                   logic       ,
		LockFailCallback<? super T, ? super O> failCallback
	);

	public <P extends Consumer<? super KeyValue<Object, O>>> P iterateLocks(P procedure);



	public static <O> LockManager<O> New(final Class<O> type)
	{
		type.getClass(); // just a simple NPE provocation

		return new Implementation<>(type);
	}



	public final class Implementation<O> implements LockManager<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// static fields //
		//////////////////

		private static final LockFailCallback<Object, Object> DEFAULT_FAIL_LOGIC = (s, d, a) -> {/**/};



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Class<O> type;

		// using an identity hashmap here is essential because identity counts, not content equality.
		private final HashTable<Object, O> lockTable = HashTable.New();

		/*
		 * data structure:
		 *
		 * hashtable -> entry
		 *
		 * entry:
		 * - subject
		 * - readWriteOwner
		 * - readWriteCount
		 * - hashNext
		 * - chain of readOnly Entries with owner and count (linearly searched)
		 */



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final Class<O> type)
		{
			super();
			this.type = type;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final Class<O> ownerType()
		{
			return this.type;
		}

		@Override
		public final synchronized O queryLockOwner(final Object object)
		{
			return this.lockTable.get(object);
		}

		@Override
		public final synchronized boolean ownsLock(final O owner, final Object object)
		{
			return this.lockTable.get(object) == owner;
		}

		@Override
		public final synchronized boolean ownsLocksAll(final O owner, final Object... objects)
		{
			final HashTable<Object, O> lockTable = this.lockTable;
			for(final Object object : objects)
			{
				if(lockTable.get(object) != owner)
				{
					return false;
				}
			}

			return true;
		}

		@Override
		public final synchronized O tryLock(final O owner, final Object object)
		{
			final KeyValue<Object, O> entry = this.lockTable.addGet(object, owner);

			return entry == null ?owner :entry.value();
		}

		/**
		 * Acquire the locks on ALL passed objects if possible or on none at all.
		 *
		 * @param owner
		 * @param objects
		 * @return the amount of locks that could not have been acquired (meaning 0 indicates success).
		 */
		@Override
		public final synchronized int reportingTryLockAllOrNone(
			final O        owner        ,
			final Object[] objects      ,
			final O[]      currentOwners
		)
		{
			// check for trivial cases (NPE provoked intentionally as a check)
			if(objects.length == 0)
			{
				return 0;
			}


			final int currentOwnersLength = currentOwners == null ? 0 : currentOwners.length;

			final HashTable<Object, O> lockTable = this.lockTable;

			int failedLockCount = 0;
			final int objectsLength = objects.length;
			for(int i = 0; i < objectsLength; i++)
			{
				final O registeredOwner;
				if((registeredOwner = lockTable.get(objects[i])) != null && registeredOwner != owner)
				{
					failedLockCount++;
				}
				if(i < currentOwnersLength && currentOwners != null)
				{
					currentOwners[i] = registeredOwner;
				}
			}

			if(failedLockCount == 0)
			{
				for(final Object object : objects)
				{
					lockTable.put(object, owner);
				}
			}

			return failedLockCount;
		}

		@Override
		public final synchronized int reportingTryLockEager(
			final O        owner        ,
			final Object[] objects      ,
			final O[]      currentOwners
		)
		{
			// check for trivial cases (NPE provoked intentionally as a check)
			if(objects.length == 0)
			{
				return 0;
			}

			final int currentOwnersLength = currentOwners == null ? 0 : currentOwners.length;

			final HashTable<Object, O> lockTable = this.lockTable;

			int failedLockCount = 0;

			final int objectsLength = objects.length;
			for(int i = 0; i < objectsLength; i++)
			{
				final KeyValue<Object, O> entry;
				if((entry = lockTable.addGet(objects[i], owner)) != null && entry.value() != owner)
				{
					failedLockCount++;
				}

				if(i < currentOwnersLength && currentOwners != null)
				{
					currentOwners[i] = entry == null ? null : entry.value();
				}
			}

			return failedLockCount;
		}

		@Override
		public final synchronized O releaseLock(final O owner, final Object object)
		{
			final O registeredOwner = this.lockTable.get(object);
			if(registeredOwner != owner)
			{
				return registeredOwner;
			}

			this.uncheckedRemove(object);
			return owner;
		}

		/**
		 * Ensures that none of the passed objects are locked for the passed owner.
		 * Does not give any guarantee that the passed objects are not locked by another owner.
		 * As a hint to existing other owner, the number of existing locks not owned by the passed owner are returned.
		 *
		 * @param owner
		 * @param objects
		 * @return the number of locks owned by some other owned.
		 */
		@Override
		public final synchronized int releaseLocksAll(final O owner, final Object... objects)
		{
			final HashTable<Object, O> lockTable = this.lockTable;

			int otherOwnerCount = 0;

			for(final Object object : objects)
			{
				final O currentLockOwner;
				if((currentLockOwner = lockTable.get(object)) == owner)
				{
					this.uncheckedRemove(object);
				}
				else if(currentLockOwner != null)
				{
					otherOwnerCount++;
				}
			}

			return otherOwnerCount;
		}

		@Override
		public final synchronized <T1, T2> O transferLock(
			final O owner, final T1 current, final T2 next, final BiProcedure<? super T1, ? super T2> logic
		)
		{
			if(this.lockTable.get(current) != owner)
			{
				throw new RuntimeException(); // EXCP proper exception required, of course
			}

			final O object2owner;
			if((object2owner = this.tryLock(owner, next)) != owner)
			{
				return object2owner;
			}

			if(logic != null)
			{
				logic.accept(current, next);
			}

			return owner;
		}

		@Override
		public final <T> boolean executeLocked(final O owner, final T object, final Consumer<? super T> logic)
		{
			return this.<T>executeLocked(owner, object, logic, DEFAULT_FAIL_LOGIC);
		}

		@Override
		public final <T> boolean executeLocked(
			final O                                      owner       ,
			final T                                      object      ,
			final Consumer<? super T>                   logic       ,
			final LockFailCallback<? super T, ? super O> failCallback
		)
		{
			final O actualOwner;
			if((actualOwner = this.tryLock(owner, object)) != owner)
			{
				failCallback.handleLockFail(object, owner, actualOwner);
				return false;
			}

			try
			{
				logic.accept(object);
				return true;
			}
			finally
			{
				// better fully re-check ownership in case of side-effects like logic releasing the lock already itself.
				this.releaseLock(owner, object);
			}
		}

		@Override
		public final synchronized <P extends Consumer<? super KeyValue<Object, O>>> P iterateLocks(final P procedure)
		{
			this.lockTable.iterate(procedure);
			return procedure;
		}

		private void uncheckedRemove(final Object object)
		{
			this.lockTable.remove(object);
		}
	}

}


