package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedSequence;

/**
 * 
 *
 */
public interface XOrderingSequence<E> extends ExtendedSequence<E>
{
	public XOrderingSequence<E> shiftTo(long sourceIndex, long targetIndex);
	
	public XOrderingSequence<E> shiftTo(long sourceIndex, long targetIndex, long length);
	
	public XOrderingSequence<E> shiftBy(long sourceIndex, long distance);
	
	public XOrderingSequence<E> shiftBy(long sourceIndex, long distance, long length);

	public XOrderingSequence<E> swap(long indexA, long indexB);
	
	public XOrderingSequence<E> swap(long indexA, long indexB, long length);

	public XOrderingSequence<E> reverse();

}
