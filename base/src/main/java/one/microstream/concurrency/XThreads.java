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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class XThreads
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	/**
	 * @see #executeSynchronized(Runnable)
	 */
	private static final Object GLOBAL_LOCK = new Object();
	
	
	/**
	 * Very simple and naive way of handling an application's concurrency globally:<br>
	 * Every logic that has concurrent modifications and/or race conditions in it has to be executed via
	 * this mechanism, making an application's concurrent parts effectively single-threaded.<br>
	 * While this is not foolproof (one critical block of logic spread over multiple calls can still
	 * create inconsistent state, waiting threads are selected randomly, etc.) and definitely not suitable
	 * for complex applications, it can be a conveniently simple, working way to make concurrency-wise
	 * simple applications sufficiently concurrency-safe.
	 * 
	 * @param <T> the return value type
	 * @param logic the logic to execute
	 * @return the supplier's value
	 */
	public static <T> T executeSynchronized(final Supplier<T> logic)
	{
		synchronized(GLOBAL_LOCK)
		{
			return logic.get();
		}
	}
	
	/**
	 * @param logic the logic to execute
	 * 
	 * @see #executeSynchronized(Supplier)
	 */
	public static void executeSynchronized(final Runnable logic)
	{
		synchronized(GLOBAL_LOCK)
		{
			logic.run();
		}
	}
	
	
	
	public static final <T extends Thread> T start(final T thread)
	{
		thread.start();
		return thread;
	}

	public static final Thread start(final Runnable runnable)
	{
		final Thread t = new Thread(runnable);
		t.start();
		return t;
	}

	

	/**
	 * Causes the current thread to sleep the specified amount of milliseconds by calling {@link Thread#sleep(long)}.
	 * Should an {@link InterruptedException} of {@link Thread#sleep(long)} occur, this method restored the
	 * interruption state by invoking {@link Thread#interrupted()} and reporting the {@link InterruptedException}
	 * wrapped in a {@link RuntimeException}.<p>
	 * The underlying rationale to this behavior is explained in an internal comment.<br>
	 * In short: generically interrupting a thread while ignoring the application/library state and logic is just
	 * as naive and dangerous as {@link Thread#stop()} is.
	 * 
	 * @param  millis
	 *         the length of time to sleep in milliseconds
	 * 
	 * @see Thread#sleep(long)
	 * @see Thread#stop()
	 */
	public static final void sleep(final long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(final InterruptedException e)
		{
			/*
			 * Explanations about the meaning of InterruptedException like the following are naive:
			 * https://stackoverflow.com/questions/3976344/handling-interruptedexception-in-java
			 * 
			 * Interrupting an application's (/library's) internal thread that has a certain purpose, state and
			 * dependency to other parts of the application state by a generic technical is pretty much the same
			 * dangerous nonsense as Thread#stop:
			 * A thread embedded in a complex context and state can't be stopped or interrupted "just like that".
			 * What should a crucial thread do on such a request? Say a thread that updates a database's lock file
			 * to indicate it is actively used. Should it just terminate and stop updating the lock file that secures
			 * the database despite the other threads still accessing the database? Surely not.
			 * Or should it prematurely write the update, because "some doesn't want to wait any longer"? Nonsense.
			 * No external interference bypassing the specific logic and ignoring the state and complexity of the
			 * application makes sense. It is pure and utter nonsense to interrupt such a thread in such a generic
			 * and ignorant way.
			 * 
			 * Whoever (in terms of program logic, of course) wants a certain thread to stop must use the proper
			 * methods to do so, that properly control the application state, etc.
			 * If there are none provided, then the thread is not supposed to be stoppable or interrupt-able.
			 * It's that simple.
			 * 
			 * If a managing layer (like the OS) wants to shut down the application, it has to use its proper
			 * interfacing means for that, but never pick out single threads and stop or interrupt them one by one.
			 * Generic interruption CAN be useful IF the logic explicitly supports it.
			 * Otherwise, this is just another JDK naivety that does more harm than good.
			 * Thread#stop has been deprecated and so should generic interruption be.
			 */
			
			// restore the interruption flag
			Thread.currentThread().interrupt();
			
			// abort the current program flow and report back the inconsistent program behavior.
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Causes the current thread to sleep the specified amount of milliseconds by calling {@link Thread#sleep(long, int)}.
	 * <p>
	 * Also see the explanations in {@link #sleep(long)}
	 * 
	 * @param  millis
	 *         the length of time to sleep in milliseconds
	 *
	 * @param  nanos
	 *         {@code 0-999999} additional nanoseconds to sleep
	 * 
	 * @see Thread#sleep(long)
	 * @see Thread#stop()
	 */
	public static final void sleep(final long millis, final int nanos)
	{
		try
		{
			Thread.sleep(millis, nanos);
		}
		catch(final InterruptedException e)
		{
			// see sleep(long) above for the explanation
			
			// restore the interruption flag
			Thread.currentThread().interrupt();
			
			// abort the current program flow and report back the inconsistent program behavior.
			throw new RuntimeException(e);
		}
	}

	public static final void executeDelayed(final long millis, final Runnable action)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				XThreads.sleep(millis);
				action.run();
			}
		}.start();
	}



	public static final String getSourcePosition()
	{
		// index 0 is always safely this method call itself, index 1 is always safely the calling context
		final String stackTraceElementString = new Throwable().getStackTrace()[1].toString();

		// every StackTraceElement string is guaranteed to be in the pattern [class].[method]([class].java:[line])
		return stackTraceElementString.substring(stackTraceElementString.indexOf('.'));
	}



	///////////////////////////////////////////////////////////////////////////
	// Throwable.getStackTraceElement workaround //
	//////////////////////////////////////////////

	// CHECKSTYLE.OFF: ConstantName: method names are intentionally unchanged

	private static final Method Throwable_getStackTraceElement = getThrowable_getStackTraceElement();

	// CHECKSTYLE.ON: ConstantName

	private static final Method getThrowable_getStackTraceElement()
	{
		try
		{
			final Method m = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
			m.setAccessible(true);
			return m;
		}
		catch(final Exception e)
		{
			return null;
		}
	}
	
	private static final Integer ONE = 1;
	public static final StackTraceElement getStackTraceElement()
	{
		try
		{
			return (StackTraceElement)Throwable_getStackTraceElement.invoke(new Throwable(), ONE);
		}
		catch(final Exception e)
		{
			// do it the slow way
			return new Throwable().getStackTrace()[1];
		}
	}
	
	public static final StackTraceElement getStackTraceElement(final Integer index)
	{
		try
		{
			return (StackTraceElement)Throwable_getStackTraceElement.invoke(new Throwable(), index);
		}
		catch(final InvocationTargetException e)
		{
			// hacky due to misconceived checked exception concept
			throw (RuntimeException)e.getCause();
		}
		catch(final Exception e)
		{
			// do it the slow way
			return new Throwable().getStackTrace()[index]; // NPE intentional
		}
	}
	
	public static StackTraceElement getStackTraceElementForDeclaringClass(final Class<?> declaringClass)
	{
		return getStackTraceElementForDeclaringClassName(declaringClass.getName());
	}
	
	public static StackTraceElement getStackTraceElementForDeclaringClassName(final String declaringClassName)
	{
		for(final StackTraceElement e : new Throwable().getStackTrace())
		{
			if(e.getClassName().equals(declaringClassName))
			{
				return e;
			}
		}
		
		return null;
	}
	
	public static String getMethodNameForDeclaringClass(final Class<?> declaringClass)
	{
		return getMethodNameForDeclaringClassName(declaringClass.getName());
	}
	
	public static String getMethodNameForDeclaringClassName(final String declaringClassName)
	{
		for(final StackTraceElement e : new Throwable().getStackTrace())
		{
			if(e.getClassName().equals(declaringClassName))
			{
				return e.getMethodName();
			}
		}
		
		return null;
	}

	public static String getCurrentMethodName()
	{
		return new Throwable().getStackTrace()[1].getMethodName();
	}

	/**
	 * A copy of the JDK's default behavior for handling ultimately uncaught exceptions, as implemented in
	 * the last fallback case of {@link ThreadGroup#uncaughtException(Thread, Throwable)}.
	 * <p>
	 * Such a method is strongly required if a custom default {@link java.lang.Thread.UncaughtExceptionHandler}
	 * only handles exceptions to some type and/or some threads and wants/needs to pass all others along to the
	 * default logic.
	 * <p>
	 * As this is a copy of the JDK's logic, it suffers the typical problems of having to be updated manually
	 * in case the JDK's logic should ever change (which is not very probable in this case).
	 *
	 * @param t the thread that caused the {@link Throwable} e.
	 * @param e the {@link Throwable} to be handled.
	 */
	public static void defaultHandleUncaughtThrowable(final Thread t, final Throwable e)
	{
		// copied from java.lang.ThreadGroup#uncaughtException, JDK 1.8.0_20
		if(e instanceof ThreadDeath)
		{
			 return; // changed for debug-friendly control flow and less parenthesis
		}
		System.err.print("Exception in thread \"" + t.getName() + "\" ");
		e.printStackTrace(System.err);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XThreads()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

