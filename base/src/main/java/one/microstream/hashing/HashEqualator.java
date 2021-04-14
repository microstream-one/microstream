package one.microstream.hashing;

import one.microstream.equality.Equalator;
import one.microstream.equality.IdentityEqualator;
import one.microstream.equality.ValueTypeEqualator;


/**
 * 
 */
public interface HashEqualator<T> extends Equalator<T>, Hasher<T>
{
	@Override
	public int hash(T object);

	@Override
	public boolean equal(T object1, T object2);



	public interface Provider<T> extends Equalator.Provider<T>
	{
		@Override
		public HashEqualator<T> provideEqualator();
	}



	public interface ImmutableHashEqualator<E>
	extends HashEqualator<E>, Hasher.ImmutableHashCode<E>
	{
		// type interface only
	}

	public interface IdentityHashEqualator<E>
	extends IdentityEqualator<E>, Hasher.IdentityHashCode<E>, ImmutableHashEqualator<E>
	{
		// type interface only
	}

	public interface ValueTypeHashEqualator<E>
	extends HashEqualator<E>, Hasher.ValueHashCode<E>, ValueTypeEqualator<E>
	{
		// type interface only
	}

	public interface ImmutableValueTypeHashEqualator<E>
	extends ImmutableHashEqualator<E>, ValueTypeHashEqualator<E>
	{
		// type interface only
	}

}
