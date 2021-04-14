package one.microstream.collections;


/**
 * 
 *
 */
public abstract class AbstractSimpleArrayCollection<E> extends AbstractSectionedArrayCollection<E>
{
	/**
	 * This is an internal shortcut method to provide fast access to the various array-backed list implementations'
	 * storage arrays.<br>
	 * The purpose of this method is to allow access to the array only for read-only procedures, never for modifying
	 * accesses.
	 * <p>
	 * The returned array is expected to contain the elements of the list in simple order from index 0 on to index
	 * (size - 1), so for example an array-backed ring list (queue) can NOT (reasonably) extend this class.
	 *
	 * @return the storage array used by the list, containing all elements in straight order.
	 */
	@Override
	protected abstract E[] internalGetStorageArray();

	protected abstract int internalSize();


	/**
	 * Workaround method to handle the generics warning at a central place instead of maintaining them at hundreds
	 * of code locations. Note that the calling logic must guarantee the type safety (see calls of this method
	 * for examples)
	 *
	 * @param subject
	 */
	@SuppressWarnings("unchecked")
	protected static <E> E[] internalGetStorageArray(final AbstractSimpleArrayCollection<?> subject)
	{
		return (E[])subject.internalGetStorageArray();
	}

}
