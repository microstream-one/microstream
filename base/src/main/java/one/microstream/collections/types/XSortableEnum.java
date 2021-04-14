package one.microstream.collections.types;

import java.util.Comparator;

/**
 * 
 *
 */
public interface XSortableEnum<E> extends XSortableSequence<E>, XGettingEnum<E>, XOrderingEnum<E>
{
	public interface Creator<E> extends XSortableSequence.Creator<E>, XGettingEnum.Creator<E>
	{
		@Override
		public XSortableEnum<E> newInstance();
	}


	// (06.07.2011 TM)FIXME: XSortableEnum: elemental shift
//	public boolean shiftTo(E element, Equalator<? super E> equalator, long targetIndex);
	
//	public boolean shiftBy(E element, Equalator<? super E> equalator, long targetIndex);
	
//	public boolean shiftToStart(E element, Equalator<? super E> equalator, long targetIndex);
	
//	public boolean shiftToEnd(E element, Equalator<? super E> equalator, long targetIndex);


	@Override
	public XSortableEnum<E> shiftTo(long sourceIndex, long targetIndex);
	@Override
	public XSortableEnum<E> shiftTo(long sourceIndex, long targetIndex, long length);
	@Override
	public XSortableEnum<E> shiftBy(long sourceIndex, long distance);
	@Override
	public XSortableEnum<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public XSortableEnum<E> swap(long indexA, long indexB);
	@Override
	public XSortableEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XSortableEnum<E> reverse();



	@Override
	public XSortableEnum<E> copy();

	@Override
	public XSortableEnum<E> toReversed();

	@Override
	public XSortableEnum<E> sort(Comparator<? super E> comparator);

}
