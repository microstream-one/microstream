package one.microstream.functional;

import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;

public final class IsNull<E> implements Predicate<E>
{
	@Override
	public final boolean test(final E e) throws ThrowBreak
	{
		return e == null;
	}

}
