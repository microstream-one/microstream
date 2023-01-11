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

import one.microstream.functional.Instantiator;


/**
 * Extended {@link Threaded} class that wraps a {@link Instantiator} instance to automatically create a new
 * instance of type E to be associated with the current {@link Thread} if {@link #get()} could not find an
 * existing association for it.
 * <p>
 * <b><u>Example</u></b>: (also note the missing initialize() method compared to {@link Threaded} example)
 * <pre>{@code
 * public class SomeClass
 * {
 *     private final Threaded<StringBuilder> sb = threadLocal(sbFactory); // conveniently short
 *
 *     // ...
 *
 *     private void doStuff()
 *     {
 *         sb.get().append("stuff"); // uses each thread's own exclusive StringBuilder instance
 *     }
 *
 *     public String toString()
 *     {
 *         return sb.get().toString(); // the current thread's constructed String
 *     }
 *
 * }
 * }</pre>
 *
 * 
 */
public class ThreadedInstantiating<E> extends Threaded<E>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Convenience / readability method that wraps the passed {@link Instantiator} instance in a new
	 * {@link ThreadedInstantiating} instance.
	 * <p>
	 * <b><u>Example</u></b>: (also note the missing initialize() method compared to {@link Threaded} example)
	 * <pre>{@code
	 * public class SomeClass
	 * {
	 *     private final Threaded<StringBuilder> sb = threadLocal(sbFactory); // conveniently short
	 *
	 *     // ...
	 *
	 *     private void doStuff()
	 *     {
	 *         sb.get().append("stuff"); // uses each thread's own exclusive StringBuilder instance
	 *     }
	 *
	 *     public String toString()
	 *     {
	 *         return sb.get().toString(); // the current thread's constructed String
	 *     }
	 *
	 * }
	 * }</pre>
	 *
	 * @param <E> the type of the instances created by the passed {@link Instantiator} instance.
	 * @param instantiator the {@link Instantiator} instance to be used to create thread local instances of type E.
	 * @return a new {@link ThreadedInstantiating} instance wrapping the passed {@link Instantiator} instance.
	 */
	public static final <E> ThreadedInstantiating<E> threaded(final Instantiator<E> instantiator)
	{
		return new ThreadedInstantiating<>(instantiator);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Instantiator<E> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Instantiates a new empty {@link ThreadedInstantiating} instance with the passed {@link Instantiator} instance
	 * and default (quite small) storage size.
	 * <p>
	 * Note that an {@link Instantiator} instance that returns the same instance of type E for more than one
	 * {@link Thread}, apart from breaking the {@link Instantiator} contract to create a new instance on every call,
	 * defeats the purpose of a {@link Threaded} in the first place.<br>
	 * Still, such behavior won't cause any (direct) error in this class (and may be reasonable in certain situations
	 * despite all concerns).
	 *
	 * @param instantiator the {@link Instantiator} instance to be used to create to be associated instances of type E.
	 * @see #ThreadedInstantiating(Instantiator, int)
	 * @throws NullPointerException if the passed {@link Instantiator} instance is {@code null}.
	 */
	public ThreadedInstantiating(final Instantiator<E> instantiator) throws NullPointerException
	{
		super();
		if(instantiator == null)
		{
			throw new NullPointerException();
		}
		this.instantiator = instantiator;
	}

	/**
	 * Instantiates a new empty {@link ThreadedInstantiating} instance with the passed {@link Instantiator} instance
	 * and a storage size of at least the passed value.
	 * <p>
	 * The created instance is guaranteed to be able to store an amount of associations equal to the passed
	 * value before a storage rebuild occurs.
	 * <p>
	 * Note that an {@link Instantiator} instance that returns the same instance of type E for more than one
	 * {@link Thread}, apart from breaking the {@link Instantiator} contract to create a new instance on every call,
	 * defeats the purpose of a {@link Threaded} in the first place.<br>
	 * Still, such behavior won't cause any (direct) error in this class (and may be reasonable in certain situations
	 * despite all concerns).
	 * <p>
	 * Also note that the internal storage size can drop below the passed value (to the same size used by
	 * {@link #ThreadedInstantiating(Instantiator)}) if at some point the optimizing algorithm detects that a smaller
	 * storage size will suffice. This is guaranteed not to happen before the storage size allocated depending on the
	 * passed value had to be increased at least once (i.e. the initial capacity remains guaranteed for the initial life-
	 * time of the created instance).
	 *
	 * @param instantiator the {@link Instantiator} instance to be used to create to be associated instances of type E.
	 * @param initialCapacity the minimal storage size the {@link ThreadedInstantiating} instance gets allocated with.
	 * @throws NullPointerException if the passed {@link Instantiator} instance is {@code null}.
	 */
	public ThreadedInstantiating(final Instantiator<E> instantiator, final int initialCapacity)
		throws NullPointerException
	{
		super(initialCapacity);
		if(instantiator == null)
		{
			throw new NullPointerException();
		}
		this.instantiator = instantiator;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	/**
	 * Returns the wrapped {@link Instantiator} instance used by this instance to create instances of
	 * type E to be associated with the current {@link Thread} if no association could have been found for it.
	 *
	 * @return the wrapped {@link Instantiator} instance.
	 */
	public Instantiator<E> getInstantiator()
	{
		return this.instantiator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Locks the wrapped {@link Instantiator} instance and uses it to create a new instance of type E. The new instance
	 * is then associated with the current {@link Thread} and returned.
	 * <p>
	 * This method is called by {@link #get()} if no association for the current {@link Thread} could have been found.
	 *
	 * @return a newly created and associated instance of type E.
	 * @see one.microstream.concurrency.Threaded#lookupMissFallbackElement()
	 * @see one.microstream.concurrency.Threaded#get()
	 */
	@Override
	protected E lookupMissFallbackElement()
	{
		final E element;

		// lock instantiator and create a new element instance
		synchronized(this.instantiator)
		{
			// lock on instantiator instead of "this" to not prolong structural lock time
			element = this.instantiator.instantiate();
		}

		// add and return the newly created instance for the current thread
		this.addForCurrentThread(element);
		return element;
	}

}
