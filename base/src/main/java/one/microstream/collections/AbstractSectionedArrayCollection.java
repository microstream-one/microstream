package one.microstream.collections;


/**
 * 
 *
 */
public abstract class AbstractSectionedArrayCollection<E> extends AbstractArrayCollection<E>
{
	/**
	 * This is an internal shortcut method to provide fast access to the various array-backed collection
	 * implementations' storage arrays.<br>
	 * The purpose of this method is to allow access to the array only for read-only procedures, never for modifying
	 * accesses.
	 * <p>
	 * The returned array is expected to contain the elements of the list in an order defined by the
	 * sectionIndices provided by {@link #internalGetSectionIndices()}
	 *
	 * @return the storage array used by the list, containing all elements in sectioned order.
	 */
	@Override
	protected abstract E[] internalGetStorageArray();

	/**
	 * Defines the array sections in which the collection's elements are organized by one or more pairs of
	 * indices in the order corresponding to the collection's logical order of its contained elements.
	 * <p>
	 * Examples:<br>
	 * The trivial example would be {0,size} in case of standard sized array collections.<br>
	 * An example for actual sectioning would be {i,array.length - 1, 0,i - 1} in case of a ring buffer list
	 * comprised of two sections: one ranging from index i to array.length - 1 (with the oldest element located at i)
	 * and one ranging from 0 to i - 1 (with the newest element located at i - 1).
	 *
	 * @return a list of index pairs defining all sections of the storage array in logical order.
	 */
	protected abstract int[] internalGetSectionIndices();

}
