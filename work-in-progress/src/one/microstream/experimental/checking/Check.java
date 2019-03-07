package one.microstream.experimental.checking;

import java.util.function.Function;

import one.microstream.functional.XFunc;
import one.microstream.util.UtilStackTrace;

@FunctionalInterface
public interface Check<T, E extends RuntimeException>
{
	public T check(T instance, Function<? super E, RuntimeException> exceptionCallback);

	public default T check(final T instance)
	{
		return this.check(instance, XFunc.passThrough());
	}







	public static <T, E extends Throwable> T notNull(final T o, final Function<Throwable, E> e) throws E
	{
		if(o != null)
		{
			return o;
		}

		final Throwable t = new NullPointerException();
		final StackTraceElement[] st = UtilStackTrace.cutStacktraceByN(t.getStackTrace(), 1);

		final E ex = e.apply(t);
		ex.setStackTrace(st);
		throw ex;
	}

}
