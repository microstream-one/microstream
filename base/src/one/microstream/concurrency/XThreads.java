package one.microstream.concurrency;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Thomas Muenz
 *
 */
public final class XThreads
{
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
	 * Causes the current thread to sleep the specified amount of milliseconds.
	 * Should an {@link InterruptedException} occur, the method returns immediately.
	 *
	 * @param millis the amount of milliseconds to sleep.
	 */
	public static final void sleep(final long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(final InterruptedException e)
		{
			// interrupted, return
			return;
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
	 * Sadly, this copy is necessary as they one again failed to modularize their default logic adequately.
	 * <p>
	 * Such a method is strongly required if a custom default {@link java.lang.Thread.UncaughtExceptionHandler}
	 * only handles exceptions of some type and/or some threads and wants/needs to pass all others along to the
	 * default logic.
	 * <p>
	 * As this is a copy of the JDK's logic, it suffers the typical problems of having to be updated manually
	 * in case the JDK's logic should ever change (which is not very probable in this case).
	 *
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



	private XThreads()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

