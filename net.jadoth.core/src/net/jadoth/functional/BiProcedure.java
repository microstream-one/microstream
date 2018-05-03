package net.jadoth.functional;

import static net.jadoth.X.notNull;

/**
 *
 * @author Thomas Muenz
 * @param <T> the transient argument type
 * @param <A> the additive argument type
 */
public interface BiProcedure<T, A> // extends BiConsumer<T, A>
{
	/* extends the ridiculously named "BiConsumer" (see Procedure JavaDoc) here for compatibility reasons
	 * causes a runtime exception during call site initilization of lambdas:
	 * java.lang.ClassFormatError:
	 * Duplicate method name&signature in class file net/jadoth/test/cql/MainTestCql$$Lambda$33
	 * at sun.misc.Unsafe.defineAnonymousClass(Native Method)
	 *
	 * As "Consumer" is and will always be plain simply wrong, it has been chosen to not extend it instead of
	 * replacing this type by the dilettantish one from JDK.
	 */

	public void accept(T t, A a);


	public default BiProcedure<T, A> then(final BiProcedure<? super T, ? super A> next)
	{
		notNull(next);
		return (final T t, final A a) ->
		{
			this.accept(t, a);
			next.accept(t, a);
		};
	}

}
