package net.jadoth.functional;

import java.util.Comparator;

import net.jadoth.hash.JadothHash;
import net.jadoth.util.Equalator;


public final class JadothEqualators
{
	public static final <E> Equalator<E> identity()
	{
		// reusing the same instances has neither memory nor performance disadvantages, only referential advantages
		return JadothHash.hashEqualityIdentity();
	}

	public static final <E> Equalator<E> value()
	{
		// reusing the same instances has neither memory nor performance disadvantages, only referential advantages
		return JadothHash.hashEqualityValue();
	}

	public static final <E> Equalator<E> asEqualator(final Comparator<? super E> comparator)
	{
		return new Equalator<E>()
		{
			@Override
			public boolean equal(final E object1, final E object2)
			{
				return comparator.compare(object1, object2) == 0;
			}
		};
	}
	@SafeVarargs
	public static final <E> Equalator<E> chain(final Equalator<? super E>... equalators)
	{
		return new Equalator.Sequence<>(equalators);
	}
	public static final <E> Equalator<E> equality(final Class<E> type)
	{
		return new Equalator<E>()
		{
			@Override
			public boolean equal(final E o1, final E o2)
			{
				return o1 == null ? o2 == null : o1.equals(o2);
			}
		};
	}



	private JadothEqualators()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
