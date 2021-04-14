package one.microstream.collections;

import java.util.function.Predicate;

import one.microstream.equality.Equalator;

final class CachedSampleEquality<E> implements Predicate<E>
{
	private final Equalator<? super E> equalator;

	E sample;

	public CachedSampleEquality(final Equalator<? super E> equalator)
	{
		super();
		this.equalator = equalator;
	}

	@Override
	public boolean test(final E e)
	{
		return this.equalator.equal(this.sample, e);
	}

}
