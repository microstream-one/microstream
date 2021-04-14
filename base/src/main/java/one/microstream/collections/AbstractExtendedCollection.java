package one.microstream.collections;


import one.microstream.collections.interfaces.ExtendedCollection;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.IndexBoundsException;


/**
 * This class is an implementation-internal for optional performance optimisation.
 * <p>
 * It is the base class for every extended collection, even if the extending class does not implement
 * {@link XAddingCollection}. Subclasses of this class that do not implement {@link XAddingCollection} will throw an
 * {@link UnsupportedOperationException} in the adding methods defined in this class.<br>
 * All code using the optimisation methods in here has to ensure that it can only be legally called for implementations
 * of {@link XAddingCollection}, for example by using {@link XAddingCollection} as the concrete parameter type.
 * <p>
 * Note that this technique of using {@link UnsupportedOperationException} is explicitly not comparable to the
 * JDK's approach like in {@link java.util.Collections#unmodifiableCollection(java.util.Collection)} where a
 * general pupose type (java.util.Collection) is implemented intentionally broken to achieve a certain reduced
 * behavior,
 * while the technique described here is a cleanly encapsuled implementation detail used in combination with proper
 * typing.
 *
 *
 * 
 *
 * @param <E>
 */
public abstract class AbstractExtendedCollection<E> implements ExtendedCollection<E>
{
	public static void validateIndex(final long bound, final long index) throws IndexBoundsException
	{
		if(index < 0)
		{
			throw new IndexBoundsException(bound, index);
		}
		if(index >= bound)
		{
			throw new IndexBoundsException(bound, index);
		}
	}

	public static void ensureFreeArrayCapacity(final int size)
	{
		// actually just checks for "==", but ">=" proved to be faster in tests (probably due to simple sign checking)
		if(size >= Integer.MAX_VALUE)
		{
			throw new ArrayCapacityException(size);
		}
	}

	// (28.06.2011 TM)FIXME: implement counting add() util methods

	protected abstract int internalCountingAddAll(E[] elements) throws UnsupportedOperationException;

	protected abstract int internalCountingAddAll(E[] elements, int offset, int length)
		throws UnsupportedOperationException;

	protected abstract int internalCountingAddAll(XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException;

	protected abstract int internalCountingPutAll(E[] elements) throws UnsupportedOperationException;

	protected abstract int internalCountingPutAll(E[] elements, int offset, int length)
		throws UnsupportedOperationException;

	protected abstract int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException;

}
