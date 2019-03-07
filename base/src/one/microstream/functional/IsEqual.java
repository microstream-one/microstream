package one.microstream.functional;

import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;

public final class IsEqual<E> implements Predicate<E>
{
	private final E sample;

	public IsEqual(final E sample)
	{
		super();
		this.sample = sample;
	}

	@Override
	public final boolean test(final E e) throws ThrowBreak
	{
		return this.sample.equals(e); // element is assumed to be not null, otherwise this class makes no sense
	}

}
