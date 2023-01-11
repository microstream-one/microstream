package one.microstream.concurrency;

/*-
 * #%L
 * microstream-base
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

import static java.lang.System.identityHashCode;
import static java.lang.Thread.currentThread;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.interfaces.ConsolidatableCollection;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.functional.Instantiator;
import one.microstream.math.XMath;
import one.microstream.reference.Referencing;


/**
 * {@link Thread} local context class that maintains Thread-to-instance associations.
 * <p>
 * This means that instances of this class can be used to make instances of type E effectively thread local and thus
 * instances of this class can be seen as a kind of "wrapper" for fields that makes them thread local as if it would
 * be a language feature.
 *
 * <p>
 * <b><u>Example</u></b>:
 * <pre>{@code
 *public class SomeClass
 *{
 *    private final Threaded<StringBuilder> sb = new Threaded<StringBuilder>();
 *
 *    // ...
 *
 *    private void initialize() // initialize for current thread
 *    {
 *        sb.set(new StringBuilder());
 *    }
 *
 *    private void doStuff()
 *    {
 *        sb.get().append("stuff"); // uses each thread's own exclusive StringBuilder instance
 *    }
 *
 *    public String toString()
 *    {
 *        return sb.get().toString(); // the current thread's constructed String
 *    }
 *
 *}
 *}</pre>
 * <p>
 * The striking feature about this class is that only those procedures are synchronized that modify the internal storage
 * commonly used for all threads, while procedures that don't modify shared structures (like {@link #get()} or
 * {@link #set} to modify an existing association) are explicitly NOT synchronized, yet still thread-safe due to
 * exploiting special characteristics of {@link Thread} identities that avoid concurrency issues (simple example:
 * by using the {@link Thread} instance itself as the hash key for associations, each thread can only find its own
 * association and never reach the others).<br>
 * This makes this class dramatically more runtime efficient than the (with all due respect, "clumsy")
 * {@link java.lang.ThreadLocal} implementation (that even unnecessarily bloats {@link Thread} instances themselves,
 * but that can't be fixed here).
 * <p>
 * See {@link ThreadedInstantiating} for an extended implementation that automatically utilizes an
 * {@link Instantiator} instance to create to be associated instances for threads that dont' have an association, yet.
 * 
 *
 * 
 */
public class Threaded<E> implements ConsolidatableCollection, OptimizableCollection, Referencing<E>
{
	/*
	 * General note on usage of Thread.currentThread():
	 * Tests showed that repeated calls of Thread.currentThread() are in fact faster than calling it once
	 * and storing the reference in a local Thread variable (storing takes time as well).
	 * As opposed to (outdated?) resources findable in the net that state (without evidence) that calls to this
	 * method are quite slow, I imagine that (modern?) -server optimisations can dramatically reduce execution
	 * time for this call (maybe to simply inline the current thread pointer value from the stack or so).
	 * In addition, the current thread reference is normally not used very often, here. Mostly 2-3 times per get(),
	 * sometimes even only once (e.g. get() call encountering a missing head entry).
	 * So all local "Thread t = currentThread()" occurrences have been substituted by repeated currentThread() calls.
	 */



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	private static <E> Entry<E>[] createSlots(final int minimumLength)
	{
		// escape condition for highest int value that can be reached by left-shifting 1.
		if(XMath.isGreaterThanOrEqualHighestPowerOf2(minimumLength))
		{
			return new Entry[XMath.highestPowerOf2_int()]; // technical magic number
		}

		int slotCount = 1; // in this case small memory need is preferable to good low-grow performance.
		while(slotCount < minimumLength)
		{
			slotCount <<= 1;
		}
		return new Entry[slotCount];
	}

	public static final <E> Threaded<E> New()
	{
		return new Threaded<>();
	}

	/**
	 * Creates a new {@link Threaded} instance and associates the passed instance of type E with the current
	 * {@link Thread}.
	 *
	 * @param <E> the element type of the instances handled by the to be created {@link Threaded} instance.
	 * @param currentThreadsElement the instance of type E to be associated with the current {@link Thread}.
	 * @return a newly created {@link Threaded} instance associating the passed element instance with the current
	 *         {@link Thread}.
	 * @see #Threaded()
	 * @see ThreadedInstantiating#threaded(Instantiator)
	 */
	public static final <E> Threaded<E> New(final E currentThreadsElement)
	{
		final Threaded<E> threaded = new Threaded<>();
		threaded.set(currentThreadsElement);
		return threaded;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private volatile Entry<E>[] slots;
	private final AtomicInteger size = new AtomicInteger();
	private volatile Consumer<E> cleanUpOperation;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Instantiates a new empty {@link Threaded} instance with default (quite small) storage size.
	 *
	 * @see #Threaded(int)
	 */
	@SuppressWarnings("unchecked")
	public Threaded()
	{
		super();
		this.slots = new Entry[1]; // in this case small memory need is preferable to good low-grow performance
		this.size.set(0);
		this.cleanUpOperation = null;
	}

	/**
	 * Instantiates a new empty {@link Threaded} instance with a storage size of at least the passed value.
	 * <p>
	 * The created instance is guaranteed to be able to store an amount of associations equals to the passed
	 * value before a storage rebuild occurs.
	 * <p>
	 * Note that the internal storage size can drop below the passed value (to the same size used by
	 * {@link #Threaded()}) if at some point the optimizing algorithm detects that a smaller storage size will
	 * suffice. This is guaranteed not to happen before the storage size allocated depending on the passed value had to
	 * be increased at least once (i.e. the initial capacity remains guaranteed for the initial life-time of the created
	 * instance).
	 *
	 * @param initialCapacity the minimal storage size the {@link Threaded} instance gets allocated with.
	 * @see #Threaded()
	 * @see #optimize()
	 */
	public Threaded(final int initialCapacity)
	{
		super();
		this.slots = Threaded.<E>createSlots(initialCapacity);
		this.size.set(0);
		this.cleanUpOperation = null;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Consumer<E> getCleanUpOperation()
	{
		return this.cleanUpOperation;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters //
	////////////

	public Threaded<E> setCleanUpOperation(final Consumer<E> cleanUpOperation)
	{
		this.cleanUpOperation = cleanUpOperation;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Returns the instance of type E associated with the current {@link Thread}.
	 * <p>
	 * If no instance is associated for the current {@link Thread} yet, a fallback reference of type E is returned,
	 * which is {@code null} in the default implementation, indicating that no associated instance could have been
	 * found.
	 *
	 * @return the instance of type E associated with the current {@link Thread}.
	 * @see #set
	 * @see #remove()
	 */
	@Override
	public E get()
	{
		final Entry<E>[] slots; // this.slots can change any time, so it may only be read once per lookup!

		// unsynchronized yet thread-safe read-only lookup algorithm on immutably used entries array
		for(Entry<E> e = (slots = this.slots)[identityHashCode(currentThread()) & slots.length - 1]; e != null; e = e.next)
		{
			if(e.get() == currentThread())
			{
				return e.value; // repeated currentThread() proved to be faster than variable
			}
		}
		return this.lookupMissFallbackElement();
	}

	/**
	 * Associates the passed instance of type E with the current {@link Thread} and returns the instance of type E
	 * that has been associated with the current {@link Thread} so far, or {@code null} if nothign has been
	 * associated with the current {@link Thread}, yet.
	 * <p>
	 * Note that repeated calls to this method for the same {@link Thread} will be significantly faster and
	 * won't block other threads as the existence of a thread-exclusive entry to be updated with the passed element is
	 * already guaranteed by the first call.
	 *
	 * @param element the instance of type E that shall be associated with the current {@link Thread}.
	 * @return the instance of type E that has been associated with the current {@link Thread} so far.
	 * @see #get()
	 * @see #remove()
	 */
	public E set(final E element)
	{
		final Entry<E>[] slots; // this.slots can change any time, so it may only be read once per lookup!

		// unsynchronized thread-safe because every thread can only find his own exclusive entry.
		for(Entry<E> e = (slots = this.slots)[identityHashCode(currentThread()) & slots.length - 1]; e != null; e = e.next)
		{
			if(e.get() == currentThread())
			{
				// repeated currentThread() proved to be faster than variable
				final E old = e.value;
				e.value = element;
				return old;
			}
		}

		// no entry yet, add element in a new entry
		this.addForCurrentThread(element);
		return null;
	}

	/**
	 * Removes the association for the current {@link Thread}, releasing and returning the reference to the instance
	 * of type E so far associated with it.
	 * <p>
	 * Note that repeated calls to this method for the same {@link Thread} will be significantly faster and
	 * won't block other threads as the removal of the thread-exclusive entry is already guaranteed by the first call.
	 *
	 * @return the instance of type E so far associated with the current {@link Thread}.
	 * @see #get()
	 * @see #set
	 */
	public E remove()
	{
		final Entry<E>[] slots; // this.slots can change any time, so it may only be read once per lookup!

		// unsynchronized thread-safe because every thread finds his own exclusive entry.
		for(Entry<E> e = (slots = this.slots)[identityHashCode(currentThread()) & slots.length - 1]; e != null; e = e.next)
		{
			if(e.get() == currentThread())
			{
				// can't directly remove found entry as storage could have been rebuilt.
				this.removeForCurrentThread(identityHashCode(currentThread())); // calculate hashCode unsynchronized.
				return e.value; // the grabbed entry's value reference is still valid, so use it to return the value.
			}
		}
		return null; // no entry found, so nothing to remove, return.
	}

	/**
	 * This method is called if the lookup in {@link #get()} didn't find an entry for the current {@link Thread} to
	 * provide a fallback reference to be returned by {@link #get()}.
	 * <p>
	 * By default, this method simply returns {@code null}, indicating that no associated instance could have been
	 * found.<br>
	 * Subclasses can override this method to provide an actual fallback instance in case of a lookup miss.<br>
	 * In combination with {@link #addForCurrentThread(Object)}, this method can be used to automatically associate the
	 * fallback instance with the current {@link Thread} in case it has no instance associated, yet.
	 * <p>
	 * See {@link ThreadedInstantiating} for an example using an {@link Instantiator} to automatically create
	 * a missing instance by using a wrapped {@link Instantiator} instance.
	 *
	 * @return {@code null}.
	 * @see #get()
	 */
	protected E lookupMissFallbackElement()
	{
		return null;
	}

	/**
	 * Adds a new entry associating the passed instance of type E with the current {@link Thread}.
	 * <p>
	 * This method is only to be called from a context where it has been already determined that this
	 * {@link Threaded} instance does not contain a corresponding entry, yet. See {@link #get()} and
	 * {@link #set(Object)} for examples.
	 *
	 * @param element the instance of type E to be associated with the current {@link Thread} in a new entry.
	 */
	protected void addForCurrentThread(final E element)
	{
		// perform thread-local work before obtaining the lock
		this.addEntry(identityHashCode(currentThread()), new Entry<>(element));
	}

	// note the tiny lock time in average case (when no rebuild is necessary)
	private synchronized void addEntry(final int threadIdHashCode, final Entry<E> entry)
	{
		final Entry<E>[] slots; // this.slots can change any time, so it may only be read once per lookup!

		// add new head entry. Should another thread have slipped in a head entry while waiting, it doesn't hurt
		(slots = this.slots)[threadIdHashCode & slots.length - 1] = entry.next(slots[threadIdHashCode & slots.length - 1]);

		// increase size and rebuild slots array if necessary
		if(this.size.getAndIncrement() == slots.length)
		{
			this.internalOptimize();
		}
	}

	// the synchronized part of remove()
	private synchronized void removeForCurrentThread(final int threadIdHashCode)
	{
		final Entry<E>[] slots;
		final Entry<E>   head ;

		if((head = (slots = this.slots)[threadIdHashCode & slots.length - 1]) != null)
		{
			if(head.get() == currentThread())
			{
				// head entry special case
				slots[threadIdHashCode & slots.length - 1] = head.next; // move up or clear
				head.next = null;
				head.value = null;
				return;
			}
			for(Entry<E> last, entry = (last = head).next; entry != null; entry = (last = entry).next)
			{
				if(entry.get() == currentThread())
				{
					last.next = entry.next; // disjoint entry and null out references to ease GC
					entry.next = null;
					entry.value = null;
					return;
				}
			}
		}

		/* Getting here is actually an error: remove() found an entry but removeForCurrentThread() did not.
		 * still a meanwhile removed entry doesn't hurt if the procedure is remove() anyway, so just return.
		 */
	}

	// only to be called from a synchronized/locked context!
	@SuppressWarnings("unchecked")
	private int internalOptimize()
	{
		final Entry<E>[] slots, buffer = new Entry[this.size.get()];
		final int slotsLength = (slots = this.slots).length;
		final Consumer<E> cleanUpOp = this.cleanUpOperation;
		int count = 0;

		// build entry buffer
		for(int i = 0; i < slotsLength; i++)
		{
			for(Entry<E> entry = slots[i]; entry != null; entry = entry.next)
			{
				if(entry.get() != null)
				{
					buffer[count++] = entry;
				}
				else if(cleanUpOp != null)
				{
					try
					{
						cleanUpOp.accept(entry.value);
					}
					catch(final Exception e)
					{
						// optimize must not fail
					}
				}
			}
		}

		// no empty entries encountered and still enough space, so no rebuild necessary
		if(count == buffer.length && count < slotsLength)
		{
			return slotsLength - count;
		}

		// rebuild slots even with same array size to get rid of empty entries
		final Entry<E>[] newSlots = Threaded.<E>createSlots(count);
		final int modulo = newSlots.length - 1;
		for(int i = 0, index; i < count; i++)
		{
			final Thread thread;
			// have to check for died threads again (sadly)
			if((thread = buffer[i].get()) == null)
			{
				if(cleanUpOp != null)
				{
					try
					{
						cleanUpOp.accept(buffer[i].value);
					}
					catch(final Exception e)
					{
						// optimize must not fail
					}
				}
				continue;
			}
			newSlots[index = identityHashCode(thread) & modulo] = new Entry<>(thread, buffer[i].value, newSlots[index]);
		}

		this.size.set(count);
		this.slots = newSlots;
		return newSlots.length - count;

		/* Notes on algorithm:
		 * - creates new Entry instances instead of reusing old ones because the chain of old entries could be
		 *   concurrently iterated by other threads. Completely rebuilding and discarding all old ones is thread safe.
		 * - can't null-out old slots array or entry chain references to ease GC for the same reason.
		 * - in the special case of Threads being keys themselves, it doesn't matter if the threads concurrently doing
		 *   lookups in get() catch the old this.slots or already the new this.slots, because both arrays are used
		 *   immutably and only their exclusive entry concerns them, but neither the newly created one for the current
		 *   thread nor the empty entries that have been discarded for the new array in this method.
		 *   It's just that at some (irrelevant) point in time, they all will have switched to the new this.slots that
		 *   has been set in this method.
		 * - this.size is not used in get(), so there can be no concurrency problem with setting it asynchronously.
		 *   size() may return out of synch values, but that is no problem either as the value of size can't be seen as
		 *   a "hard fact" in the first place due to weakly referencing entries and unpredictably occurring optimisation.
		 */
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Optimizes the internal storage of this {@link Threaded} instance by removing all entries that no longer
	 * reference an existing thread, determining the new required storage size afterwards and rebuilding the storage
	 * if necessary.
	 * <p>
	 * Note that determining dead entries takes a certain amount of time, so repeated calls of this method
	 * each cost a certain amount of time, also much lower than an actual rebuild takes.
	 * <p>
	 * The method {@link #set(Object)} automatically causes optimisation if it becomes necessary to enlarge the
	 * internal storage. This also leads to automatically shrinking the internal storage if possible.
	 * That is if the analysis of internal storage space prior to enlarging it discards enough empty entries so that
	 * a smaller internal storage suffices.<br>
	 * As a consequence, this method has hardly to be called at all. The only situation where explicit calls
	 * of this method are needed is if the {@link Threaded} instance maintains many (potentially obsolote)
	 * associations but no further {@link #set(Object)} procedure is performed that would eventually shrink the
	 * storage automatically.
	 * <p>
	 * This method returns the amount of associations (threads) that can be added before the internal storage
	 * has to be optimized again, as defined by {@link OptimizableCollection#optimize()}.
	 *
	 * @return amount of associations (threads) that can be added before the internal storage has to be optimized again.
	 * @see #consolidate()
	 */
	@Override
	public synchronized long optimize()
	{
		return this.internalOptimize();
	}

	/**
	 * Consolidates the internal storage of this {@link Threaded} instance (i.e. removes all entries that no longer
	 * reference an existing {@link Thread}) by optimizing the internal storage.
	 * <p>
	 * See {@link #optimize()} for further details.
	 * <p>
	 * In contrast to {@link #optimize()}, this method returns the number of empty entries discarded in the process.
	 *
	 * @return the number of empty entries that have been removed in the process.
	 * @see #optimize()
	 */
	@Override
	public synchronized long consolidate()
	{
		final int oldSize = this.size.get();
		this.internalOptimize();
		return oldSize - this.size.get();
	}

	/**
	 * Returns {@code true} if this {@link Threaded} instance contains no entries for associating instances
	 * of type E, otherwise <code>false</code>.
	 * <p>
	 * Note that empty entries (entries that no longer reference an existing thread) do still count as contained
	 * entries. As a consequence, in order to determine if {@link Threaded} instance is actually empty regarding
	 * still valid entries, {@link #consolidate()} should be called immediately prior to calling this method.
	 *
	 * @return {@code true} if this {@link Threaded} instance contains no associations.
	 * @see #size()
	 * @see #consolidate()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.size.get() == 0;
	}

	/**
	 * The amount of associations contained in this {@link Threaded} instance.
	 * <p>
	 * Note that empty entries (entries that no longer reference an existing thread) do still count as contained
	 * entries. As a consequence, in order to determine the actual amount of valid entries, {@link #consolidate()}
	 * should be called immediately prior to calling this method.
	 *
	 * @return the amount of associations contained in this {@link Threaded} instance.
	 * @see #isEmpty()
	 * @see #consolidate()
	 */
	@Override
	public long size()
	{
		return this.size.get();
	}

	public synchronized boolean containsSearched(final Predicate<? super E> predicate)
	{
		final Entry<E>[] slots;
		final int slotsLength = (slots = this.slots).length;
		for(int i = 0; i < slotsLength; i++)
		{
			for(Entry<E> entry = slots[i]; entry != null; entry = entry.next)
			{
				if(predicate.test(entry.value))
				{
					return true;
				}
			}
		}
		
		return false;
	}



	private static final class Entry<E> extends WeakReference<Thread>
	{
		E value;
		Entry<E> next;

		Entry(final E value)
		{
			super(currentThread());
			this.value = value;
			this.next = null;
		}

		Entry(final Thread thread, final E value, final Entry<E> next)
		{
			super(thread);
			this.value = value;
			this.next = next;
		}

		Entry<E> next(final Entry<E> next)
		{
			this.next = next;
			return this;
		}

	}

}
