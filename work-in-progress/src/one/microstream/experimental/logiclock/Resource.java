package one.microstream.experimental.logiclock;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.functional.Action;
import one.microstream.functional.XFunc;

/**
 * /!\ WORK IN PROGRESS PROTOTYPE - DO NOT USE /!\
 * <p>
 * Concurrent logic modelling abstraction representing a resource that shall manage concurrent accesses to it.
 * <p>
 * A resource can either be a node in a resource tree (referencing several other resources) or an actual resource
 * (leaf in the tree).
 * <p>
 * An owner must and can be anything that represents one exclusive participant of the resource management context.
 * E.g. a {@link Thread} or a business logic worker/handler instance that is exclusive to a thread (represents a thread)
 * or maybe even an entity that is shared amongt multiple threads which do their synchronization internally.
 * <p>
 * A resource can be locked in two way:<br>
 * - exclusively (typically, but not always, for reading and writing)<br>
 * - shared (typically, but not always, for reading only)<br>
 * In order to acquire an exclusive lock to a resource, no other participant<br>
 * <p>
 * This is a modernized approach to the somewhat old and clumsy {@link Lock} concept, as it fixes many of its weaknesses.<br>
 * E.g.:<br>
 * - functional programming to properly encapsulate/separate releasing (or unlocking) responsibility on the responsible
 *   technical layer instead of in the user logic layer (see {@link Lock} class description)<br>
 * - optional functional retry handler for maximal flexibility in logic interoperability (e.g. owner communication)
 *   instead of simply just a concrete optional time parameter<br>
 * - resources can be locked multiple times and get released accordingly<br>
 * - naming reflecting its role in the programm logic, not its technical implementation
 *   (JDK guys always mess those two up in public APIs)<br>
 *
 * @author Thomas Muenz
 *
 * @param <O> type of the owner
 */
// (01.07.2013 TM)XXX: Java 8: turn into interface with default methods
public abstract class Resource<O>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <T> Resource<T> New()
	{
		return new Leaf<>();
	}

	@SafeVarargs
	public static final <T> Resource<T> New(final Resource<T>... children)
	{
		return new Node<>(children); // (30.06.2013)XXX: maybe defensive copy here
	}

	public static final <T> Resource<T> Shared(final Resource<T> lock)
	{
		return lock instanceof Shared ? lock : new Shared<>(lock);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected abstract void acquireLock(O owner, Predicate<? super Resource<? extends O>> retryCallback) throws LockException;

	protected abstract void releaseLock(O owner);

	protected abstract void acquireSharedLock(O owner, Predicate<? super Resource<? extends O>> retryCallback) throws LockException;

	protected abstract void releaseSharedLock(O owner);


	public final void execute(final O owner, final Action action, final Predicate<? super Resource<? extends O>> retryCallback) throws LockException
	{
		this.acquireLock(owner, retryCallback);
		try
		{
			action.execute();
		}
		finally {
			this.releaseLock(owner);
		}
	}

	public final void execute(final O owner, final Action action) throws LockException
	{
		this.execute(owner, action, XFunc.none()); // actually more like "never" or "no" instead of none.
	}

	public abstract void iterateLocks(Consumer<? super Resource<? super O>> procedure);

	public abstract void iterateSharingOwners(Consumer<? super O> procedure);

	public abstract O    exclusiveOwner();



	static final class Node<O> extends Resource<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Resource<O>[] children;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Node(final Resource<O>[] children)
		{
			super();
			this.children = children;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public final synchronized O exclusiveOwner()
		{
			/* (01.07.2013 TM)XXX: Resource.Node#exlusiveOwner() ?
			 * What to return here? this is a problem...
			 * Cannot simply return the owner of the first child.
			 * Would be consistent with locking algorithm but would return false information if
			 * no exclusive lock is held through this node at all but through something else.
			 *
			 * It's probably correct to return null, as a node does not have any specific state or owner on its own.
			 * It also cannot throw a LockException that would require a trying participant to query the owner.
			 * Only actual resources (leafs) throw exceptions and they do have a proper exclusive owner.
			 */
			return null;
		}

		@Override
		protected final void acquireLock(final O owner, final Predicate<? super Resource<? extends O>> retryCallback) throws LockException
		{
			// length is assumed to be small in most cases (2-5 elements), so local cache variable hardly pays off
			int i = 0;
			try
			{
				// acquire lock of all children
				while(i < this.children.length)
				{
					this.children[i].acquireLock(owner, retryCallback);
					i++;
				}
			}
			catch(final LockException e)
			{
				// release all locks acquired so far
				while(--i >= 0)
				{
					this.children[i].releaseLock(owner);
				}
				// pass along exception
				throw e;
			}
		}

		@Override
		protected final void releaseLock(final O owner)
		{
			// length is assumed to be small in most cases (2-5 elements), so local cache variable hardly pays off
			for(int i = 0; i < this.children.length; i++)
			{
				this.children[i].releaseLock(owner);
			}
		}

		@Override
		protected final void acquireSharedLock(
			final O                                        owner           ,
			final Predicate<? super Resource<? extends O>> retryCallback
		)
			throws LockException
		{
			// length is assumed to be small in most cases (2-5 elements), so local cache variable hardly pays off
			int i = 0;
			try
			{
				// acquire lock of all children
				while(i < this.children.length)
				{
					this.children[i].acquireSharedLock(owner, retryCallback);
					i++;
				}
			}
			catch(final LockException e)
			{
				// release all locks acquired so far
				while(--i >= 0)
				{
					this.children[i].releaseSharedLock(owner);
				}
				// pass along exception
				throw e;
			}
		}

		@Override
		protected final void releaseSharedLock(final O owner)
		{
			// length is assumed to be small in most cases (2-5 elements), so local cache variable hardly pays off
			for(int i = 0; i < this.children.length; i++)
			{
				this.children[i].releaseSharedLock(owner);
			}
		}

		@Override
		public final void iterateLocks(final Consumer<? super Resource<? super O>> procedure)
		{
			for(int i = 0; i < this.children.length; i++)
			{
				this.children[i].iterateLocks(procedure);
			}
		}

		@Override
		public final void iterateSharingOwners(final Consumer<? super O> procedure)
		{
			for(int i = 0; i < this.children.length; i++)
			{
				this.children[i].iterateSharingOwners(procedure);
			}
		}

	}



	static final class Shared<O> extends Resource<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final Resource<O> actual;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Shared(final Resource<O> actual)
		{
			super();
			this.actual = actual;
		}

		@Override
		public O exclusiveOwner()
		{
			return this.actual.exclusiveOwner();
		}

		@Override
		protected final void acquireLock(
			final O                                        owner           ,
			final Predicate<? super Resource<? extends O>> retryCallback
		)
			throws LockException
		{
			this.actual.acquireSharedLock(owner, retryCallback);
		}

		@Override
		protected final void releaseLock(final O owner)
		{
			this.actual.releaseSharedLock(owner);
		}

		@Override
		protected final void acquireSharedLock(
			final O                                        owner           ,
			final Predicate<? super Resource<? extends O>> retryCallback
		)
			throws LockException
		{
			this.actual.acquireSharedLock(owner, retryCallback);
		}

		@Override
		protected final void releaseSharedLock(final O owner)
		{
			this.actual.releaseSharedLock(owner);
		}

		@Override
		public final void iterateLocks(final Consumer<? super Resource<? super O>> procedure)
		{
			this.actual.iterateLocks(procedure);
		}

		@Override
		public final void iterateSharingOwners(final Consumer<? super O> procedure)
		{
			this.actual.iterateSharingOwners(procedure);
		}

	}

	static final class Entry<T>
	{
	          int      level; // level is kind of similar to recursion levels, so 2 billion should very well suffice.
		final T        owner;
		      Entry<T> link ;

		Entry(final T owner, final Entry<T> next)
		{
			super();
			this.owner = owner;
			this.link  = next ;
			this.level = 1    ;
		}

	}


	static final class Leaf<O> extends Resource<O>
	{
		@SuppressWarnings("unchecked")
		private static <O> Entry<O>[] newSlots(final int length)
		{
			return new Entry[length];
		}


		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private O   exclusiveOwner = null;
		private int level          = 0   ; // level is kind of similar to recursion levels, so 2 billion should very well suffice.

		private Entry<O>[] hashSlots = newSlots(1);
		private int        hashRange = this.hashSlots.length - 1;
		private int        size      = 0;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Leaf()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private boolean isSingleEntry(final O owner)
		{
			final Entry<O> e;
			return this.size == 1
				&& (e = this.hashSlots[this.hashRange & System.identityHashCode(owner)]) != null
				&& e.owner == owner
			;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized O exclusiveOwner()
		{
			return this.exclusiveOwner;
		}

		@Override
		protected final synchronized void acquireLock(
			final O                                        owner        ,
			final Predicate<? super Resource<? extends O>> retryCallback
		)
			throws LockException
		{
			do {
				if(this.size == 0 || this.isSingleEntry(owner))
				{
					if(this.exclusiveOwner == null)
					{
						// lock newly acquired
						this.exclusiveOwner = owner;
						this.level = 1;
						return;
					}
					if(this.exclusiveOwner == owner)
					{
						// lock acquired again (recursively reentrant)
						this.level++;
						return;
					}
				}
			}
			while(retryCallback.test(this));

			// retry evaluation yielded false, hence throw exception
			throw new LockException(this);
		}

		@Override
		protected final synchronized void releaseLock(final O owner)
		{
			if(--this.level == 0)
			{
				this.exclusiveOwner = null;
			}
		}

		private void synchRebuildHashTable(final int newLength)
		{
			if(this.hashSlots.length >= newLength || newLength <= 0)
			{
				return;
			}

			final int newRange = newLength - 1;
			final Entry<O>[] oldSlots = this.hashSlots;
			final Entry<O>[] newSlots = newSlots(newLength);
			for(int i = 0; i < oldSlots.length; i++)
			{
				if(oldSlots[i] == null)
				{
					continue;
				}
				for(Entry<O> next, entry = oldSlots[i]; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[System.identityHashCode(entry.owner) & newRange];
					newSlots[System.identityHashCode(entry.owner) & newRange] = entry;
				}
			}
			this.hashSlots = newSlots;
			this.hashRange = newRange;
		}

		private void synchAddSharedLock(final O owner)
		{
			this.hashSlots[this.hashRange & System.identityHashCode(owner)] =
				new Entry<>(owner, this.hashSlots[this.hashRange & System.identityHashCode(owner)])
			;

			// check for hash table upsizing
			if(++this.size >= this.hashRange)
			{
				this.synchRebuildHashTable((int)(this.hashSlots.length * 2.0f));
			}
		}

		@Override
		protected final synchronized void acquireSharedLock(
			final O owner,
			final Predicate<? super Resource<? extends O>> retryCallback
		)
			throws LockException
		{
			do{
				if(this.exclusiveOwner == null || this.exclusiveOwner == owner)
				{
					for(Entry<O> e; (e = this.hashSlots[this.hashRange & System.identityHashCode(owner)]) != null; e = e.link)
					{
						if(e.owner == owner)
						{
							e.level++;
							return;
						}
					}
					this.synchAddSharedLock(owner);
					return;
				}
			}
			while(retryCallback.test(this));

			// retry evaluation yielded false, hence throw exception
			throw new LockException(this);
		}

		@Override
		protected final synchronized void releaseSharedLock(final O owner)
		{
			// note: cannot be null since owner is guaranteed to having been added beforehand
			Entry<O> last = this.hashSlots[this.hashRange & System.identityHashCode(owner)];
			if(last.owner == owner)
			{
				if(last.level == 1)
				{
					this.hashSlots[this.hashRange & System.identityHashCode(owner)] = last.link;
					this.size--;
				}
				else
				{
					last.level--;
				}
			}
			else
			{
				for(Entry<O> e; (e = last.link) != null; last = e)
				{
					if(e.owner == owner)
					{
						if(e.level == 1)
						{
							last = e.link;
							this.size--;
						}
						else
						{
							e.level--;
						}
						break;
					}
				}
			}

			// check for hash table downsizing
			if(this.size << 1 < this.hashSlots.length)
			{
				this.synchRebuildHashTable(this.hashSlots.length >> 1);
			}
		}

		@Override
		public final void iterateLocks(final Consumer<? super Resource<? super O>> procedure)
		{
			procedure.accept(this);
		}

		@Override
		public final synchronized void iterateSharingOwners(final Consumer<? super O> procedure)
		{
			final Entry<O>[] hashSlots = this.hashSlots;
			for(int i = 0; i < hashSlots.length; i++)
			{
				if(hashSlots[i] == null)
				{
					continue;
				}
				for(Entry<O> e = hashSlots[i]; e != null; e = e.link)
				{
					procedure.accept(e.owner);
				}
			}
		}

	}



	public static final class LockException extends RuntimeException
	{
		private final Resource<?> subject;

		public LockException(final Resource<?> subject)
		{
			super();
			this.subject = subject;
		}

		public final Resource<?> subject()
		{
			return this.subject;
		}

		@Override
		public final synchronized Throwable fillInStackTrace()
		{
			// means of program flow not just a debugging, hence expensive debugging logic turned off.
//			return super.fillInStackTrace();
			return this;
		}

	}

}
