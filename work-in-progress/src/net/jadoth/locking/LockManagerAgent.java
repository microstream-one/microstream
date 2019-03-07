package net.jadoth.locking;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

interface LockManagerAgent<O> extends LockOwnerTypeHolder<O>
{
	public LockManager<O> master();

	public O owner();

	@Override
	public default Class<O> ownerType()
	{
		return this.master().ownerType();
	}

	public default O tryLock(final Object object)
	{
		return this.master().tryLock(this.owner(), object);
	}

	public default O releaseLock(final Object object)
	{
		return this.master().releaseLock(this.owner(), object);
	}

	public default O transferLock(final Object current, final Object next)
	{
		return this.master().transferLock(this.owner(), current, next);
	}

	public default <T> boolean executeLocked(final T object, final Consumer<? super T> logic)
	{
		return this.master().executeLocked(this.owner(), object, logic);
	}

	public default boolean ownsLock(final Object object)
	{
		return this.master().ownsLock(this.owner(), object);
	}

	public default boolean ownsLocksAll(final Object... objects)
	{
		return this.master().ownsLocksAll(this.owner(), objects);
	}

	public default int tryLockAllOrNone(final Object... objects)
	{
		return this.master().tryLockAllOrNone(this.owner(), objects);
	}

	public default int tryLockEager(final Object... objects)
	{
		return this.master().tryLockEager(this.owner(), objects);
	}

	public default int reportingTryLockAllOrNone(final Object[] objects, final O[] currentOwners)
	{
		return this.master().reportingTryLockAllOrNone(this.owner(), objects, currentOwners);
	}

	public default int reportingTryLockEager(final Object[] objects, final O[] currentOwners)
	{
		return this.master().reportingTryLockEager(this.owner(), objects, currentOwners);
	}

	public default int releaseLocksAll(final Object... objects)
	{
		return this.master().releaseLocksAll(this.owner(), objects);
	}

	public default <T> boolean executeLocked(
		final T                                      object      ,
		final Consumer<? super T>                   logic       ,
		final LockFailCallback<? super T, ? super O> failCallback
	)
	{
		return this.master().executeLocked(this.owner(), object, logic, failCallback);
	}




	public static <O> LockManagerAgent<O> New(final LockManager<O> master, final O owner)
	{
		return new Implementation<>(
			notNull(master),
			notNull(owner)
		);
	}

	public class Implementation<O> implements LockManagerAgent<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final LockManager<O> master;
		private final O              owner ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final LockManager<O> master, final O owner)
		{
			super();
			this.master = master;
			this.owner  = owner ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public O owner()
		{
			return this.owner;
		}

		@Override
		public LockManager<O> master()
		{
			return this.master;
		}

		@Override
		public String toString()
		{
			return this.getClass()+" "+this.master.toString()+" for owner "+this.owner.toString();
		}

		// this is how a class should ideally look like: immutable state plus simple specific logic. Rest in interfaces.

	}

}
