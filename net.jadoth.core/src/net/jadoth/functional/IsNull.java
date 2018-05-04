package net.jadoth.functional;

import java.util.function.Predicate;

import net.jadoth.branching.ThrowBreak;

public final class IsNull<E> implements Predicate<E>
{
	@Override
	public final boolean test(final E e) throws ThrowBreak
	{
		return e == null;
	}

}
