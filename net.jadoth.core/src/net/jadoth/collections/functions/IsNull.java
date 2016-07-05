package net.jadoth.collections.functions;

import java.util.function.Predicate;

import net.jadoth.util.branching.ThrowBreak;

public final class IsNull<E> implements Predicate<E>
{
	@Override
	public final boolean test(final E e) throws ThrowBreak
	{
		return e == null;
	}

}
