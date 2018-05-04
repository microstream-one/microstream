package net.jadoth.functional;

import java.util.function.Predicate;

import net.jadoth.equality.Equalator;

public final class IsCustomEqual<E> implements Predicate<E>
{
	private final Equalator<? super E> equalator;
	private final E                    sample   ;

	public IsCustomEqual(final Equalator<? super E> equalator, final E sample)
	{
		super();
		this.equalator = equalator;
		this.sample    = sample   ;
	}

	@Override
	public final boolean test(final E e)
	{
		return this.equalator.equal(this.sample, e);
	}

}
