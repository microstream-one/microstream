package one.microstream.functional;

import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;

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
