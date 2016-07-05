package net.jadoth.experimental.checking;

import java.util.function.Function;

import net.jadoth.functional.JadothFunctions;
import net.jadoth.util.JadothExceptions;

@FunctionalInterface
public interface Check<T, E extends RuntimeException>
{
	public T check(T instance, Function<? super E, RuntimeException> exceptionCallback);

	public default T check(final T instance)
	{
		return this.check(instance, JadothFunctions.passthrough());
	}







	public static <T, E extends Throwable> T notNull(final T o, final Function<Throwable, E> e) throws E
	{
		if(o != null)
		{
			return o;
		}

		final Throwable t = new NullPointerException();
		final StackTraceElement[] st = JadothExceptions.cutStacktraceByN(t.getStackTrace(), 1);

		final E ex = e.apply(t);
		ex.setStackTrace(st);
		throw ex;
	}

}
