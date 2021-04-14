package one.microstream.collections;

import one.microstream.X;
import one.microstream.collections.interfaces.Sized;
import one.microstream.math.XMath;


/**
 * 
 *
 */
public abstract class AbstractArrayCollection<E> extends AbstractExtendedCollection<E> implements Sized
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// internal marker object for marking to be removed slots for batch removal and null ambiguity resolution
	private static final transient Object MARKER = new Object();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	protected static final <E> E marker()
	{
		return (E)MARKER;
	}


	@SuppressWarnings("unchecked")
	protected static final <E> E[] newArray(final int length)
	{
		return (E[])new Object[length];
	}

	protected static final <E> E[] newArray(final int length, final E[] oldData, final int oldDataLength)
	{
		final E[] newArray = newArray(length);
		System.arraycopy(oldData, 0, newArray, 0, oldDataLength);
		return newArray;
	}

	public static final int pow2BoundMaxed(final long n)
	{
		return XMath.pow2BoundMaxed(X.checkArrayRange(n));
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * This is an internal shortcut method to provide fast access to the various array-backed list implementations'
	 * storage arrays.<br>
	 * The purpose of this method is to allow access to the array only for read-only procedures, never for modifying
	 * accesses.
	 *
	 * @return the storage array used by the list, containing all elements in straight order.
	 */
	protected abstract E[] internalGetStorageArray();

//	public int scan(final Predicate<? super E> predicate)
//	{
//		return AbstractArrayStorage.forwardScan(this.internalGetStorageArray(), this.size(), predicate);
//	}

}
