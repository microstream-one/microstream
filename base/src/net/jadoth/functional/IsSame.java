package net.jadoth.functional;

import java.util.function.Predicate;

import net.jadoth.branching.ThrowBreak;

public final class IsSame<E> implements Predicate<E>
{
	private final E sample;

	public IsSame(final E sample)
	{
		super();
		this.sample = sample;
	}

	@Override
	public final boolean test(final E e) throws ThrowBreak
	{
		return this.sample == e;
	}

}
