package one.microstream.collections.types;


/**
 * 
 *
 */
public interface XOrderingEnum<E> extends XOrderingSequence<E>
{
	// (06.07.2011 TM)FIXME: XOrderingEnum: elemental shift
//	public boolean shiftTo(E element, long targetIndex);
	
//	public boolean shiftBy(E element, long targetIndex);
	
//	public boolean shiftToStart(E element, long targetIndex);
	
//	public boolean shiftToEnd(E element, long targetIndex);


	@Override
	public XOrderingEnum<E> shiftTo(long sourceIndex, long targetIndex);
	@Override
	public XOrderingEnum<E> shiftTo(long sourceIndex, long targetIndex, long length);
	@Override
	public XOrderingEnum<E> shiftBy(long sourceIndex, long distance);
	@Override
	public XOrderingEnum<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public XOrderingEnum<E> swap(long indexA, long indexB);
	@Override
	public XOrderingEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XOrderingEnum<E> reverse();

}
