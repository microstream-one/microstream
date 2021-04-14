package one.microstream.collections.types;

import java.util.ListIterator;

import one.microstream.collections.interfaces.ExtendedList;
import one.microstream.collections.old.OldList;

/**
 * 
 *
 */
public interface XGettingList<E> extends XGettingSequence<E>, XGettingBag<E>, ExtendedList<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingList<E> newInstance();
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableList<E> immure();

	// java.util.List List Iterators
	public ListIterator<E> listIterator();
	
	public ListIterator<E> listIterator(long index);

	@Override
	public OldList<E> old();

	@Override
	public XGettingList<E> copy();

	@Override
	public XGettingList<E> toReversed();

	@Override
	public XGettingList<E> view();
	
	@Override
	public XGettingList<E> view(long lowIndex, long highIndex);

	@Override
	public XGettingList<E> range(long fromIndex, long toIndex);

}
