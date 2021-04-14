package one.microstream.collections.types;

import java.util.Comparator;

import one.microstream.collections.sorting.Sortable;

/**
 * 
 *
 */
public interface XSortableSequence<E> extends XGettingSequence<E>, Sortable<E>, XOrderingSequence<E>
{
	// (01.12.2011 TM)XXX: what about XOrderingList? At least for content behavior ensurance.

	public interface Creator<E> extends XGettingSequence.Factory<E>
	{
		@Override
		public XGettingSequence<E> newInstance();
	}



	@Override
	public XSortableSequence<E> shiftTo(long sourceIndex, long targetIndex);
	
	@Override
	public XSortableSequence<E> shiftTo(long sourceIndex, long targetIndex, long length);
	
	@Override
	public XSortableSequence<E> shiftBy(long sourceIndex, long distance);
	
	@Override
	public XSortableSequence<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public XSortableSequence<E> swap(long indexA, long indexB);
	
	@Override
	public XSortableSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XSortableSequence<E> reverse();

	@Override
	public XSortableSequence<E> sort(Comparator<? super E> comparator);

	@Override
	public XSortableSequence<E> copy();

	@Override
	public XSortableSequence<E> toReversed();

}
