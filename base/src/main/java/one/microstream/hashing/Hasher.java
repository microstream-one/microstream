package one.microstream.hashing;


/**
 * 
 *
 */
public interface Hasher<T>
{
	public int hash(T object);



	/**
	 * Type interface to indicate that the implementation uses {@link Object#hashCode()} (that actually only makes
	 * sense for proper value types, not for entity types).
	 *
	 * 
	 *
	 * @param <E>
	 */
	public interface ValueHashCode<E> extends Hasher<E>
	{
		// type interface only
	}

	/**
	 * Type interface to indicate that the implementing {@link Hasher} implementation will always return
	 * the same hash value for the same object.
	 * <br>
	 * This is true for immutable objects (such as instances of {@link String}) or for the identity hash code provided
	 * by {@link System#identityHashCode(Object)}.<br>
	 * The behavior can also be achieved by caching a once created hash code object-externally in the {@link Hasher}
	 * implementation to ensure unchanging hash codes even for objects that are mutable in terms of their
	 * {@link Object#equals(Object)} implemententation.
	 * <p>
	 * The purpose of this marker interface is to indicate that using an implementation of it will not create
	 * hash values that will mess up a hash-based element distribution, which allows certain algorithm optimisations,
	 * for example in hashing collections.
	 *
	 *
	 * 
	 *
	 * @param <E>
	 * @see IdentityHashCode
	 */
	public interface ImmutableHashCode<E> extends Hasher<E>
	{
		// type interface only
	}

	/**
	 * Type interface to indicate that the implementation uses {@link System#identityHashCode(Object)}.
	 *
	 * 
	 *
	 * @param <E>
	 */
	public interface IdentityHashCode<E> extends ImmutableHashCode<E>
	{
		// type interface only
	}
	
}
