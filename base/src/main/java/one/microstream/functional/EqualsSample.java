package one.microstream.functional;

import java.util.function.Predicate;

import one.microstream.equality.Equalator;

final class EqualsSample<T> implements Predicate<T>
{
	private final T sample;
	private final Equalator<? super T> equalator;

	public EqualsSample(final T sample, final Equalator<? super T> equalator)
	{
		super();
		this.sample = sample;
		this.equalator = equalator;
	}

	@Override
	public boolean test(final T e) throws RuntimeException
	{
		return this.equalator.equal(this.sample, e);
	}

}
