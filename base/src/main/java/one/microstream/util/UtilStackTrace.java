package one.microstream.util;

import static java.lang.System.arraycopy;

public class UtilStackTrace
{
	public static final <T extends Throwable> T cutStacktraceTo(final T t, final Class<?> c, final String methodName)
	{
		final StackTraceElement[] stackTrace = t.getStackTrace();
		final String cName = c.getName();

		int stackTracesToSkip = 0;
		for(final StackTraceElement ste : stackTrace)
		{
			if(ste.getClassName().equals(cName) && ste.getMethodName().equals(methodName))
			{
				break;
			}
			stackTracesToSkip++;
		}
		return UtilStackTrace.cutStacktraceByN(t, stackTracesToSkip);
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

}
