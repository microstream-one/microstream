package net.jadoth.util;

import static java.lang.System.arraycopy;
import net.jadoth.exceptions.WrapperRuntimeException;

public class JadothExceptions
{
	public static final <T extends Throwable> T addSuppressed(final T throwable, final Throwable suppressed)
	{
		throwable.addSuppressed(suppressed);
		return throwable;
	}

	public static final <T extends Throwable> T addSuppressed(final T throwable, final Throwable... suppresseds)
	{
		/* (19.07.2012 TM)NOTE: sometimes I tend to believe that the JDK developers decrease usabilty on purpose
		 * Why the heck can't addSuppressed() return the instance in order to keep local code tight and readable?
		 * And why can't there be an additional VarArgs addSuppressed()?
		 * Why does one have to code EVERYTHING above the absolute necessary manually?
		 */
		for(final Throwable suppressed : suppresseds)
		{
			throwable.addSuppressed(suppressed);
		}
		return throwable;
	}

	public static final <T extends Throwable> T cutStacktraceTo(final T t, final Class<?> c, final String methodname)
	{
		final StackTraceElement[] stackTrace = t.getStackTrace();
		final String cName = c.getName();

		int stackTracesToSkip = 0;
		for(final StackTraceElement ste : stackTrace)
		{
			if(ste.getClassName().equals(cName) && ste.getMethodName().equals(methodname))
			{
				break;
			}
			stackTracesToSkip++;
		}
		return JadothExceptions.cutStacktraceByN(t, stackTracesToSkip);
	}

	public static final <T extends Throwable> T cutStacktraceByOne(final T throwable)
	{
		final StackTraceElement[] st1, st2;
		arraycopy(st1 = throwable.getStackTrace(), 1, st2 = new StackTraceElement[st1.length - 1], 0, st1.length - 1);
		throwable.setStackTrace(st2);
		return throwable;
	}

	public static <T extends Throwable> T cutStacktraceByN(final T throwable, final int n)
	{
		throwable.setStackTrace(cutStacktraceByN(throwable.getStackTrace(), n));
		return throwable;
	}

	public static StackTraceElement[] cutStacktraceByN(final StackTraceElement[] stacktrace, final int n)
	{
		// note: n < 0 is inherently checked by arraycopy. n == 0 is nonsense, but harmless
		final StackTraceElement[] st2;
		arraycopy(stacktrace, n, st2 = new StackTraceElement[stacktrace.length - n], 0, stacktrace.length - n);
		return st2;
	}

	/**
	 * Wraps the passed {@link Exception} instance in an {@link WrapperRuntimeException} if it is not already of type
	 * {@link RuntimeException}. This is necessary to compensate / workaround the misdesigned checked exeptions for
	 * development with higher abstraction and modularization (e.g. functional programming) than the checked exception
	 * designers were able to forsee at the time.
	 *
	 * @param e
	 * @return the passed exception in runtime form.
	 * @see WrapperRuntimeException
	 */
	public RuntimeException workaroundDamnedCheckedExceptions(final Exception e)
	{
		return e instanceof RuntimeException
			? (RuntimeException)e
			: new WrapperRuntimeException(e)
		;
	}

}
