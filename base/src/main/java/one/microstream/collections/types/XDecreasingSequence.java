package one.microstream.collections.types;

import java.util.Comparator;

/**
 * Intermediate list type that combines all list aspects except increasing (adding and inserting), effectively causing
 * instances of this list type to maintain its size or shrink, but never grow.
 * <p>
 * This type is primarily used for the values list of a map, which can offer all functionality except adding
 * values (without mapping it to a key).
 *
 * 
 */
public interface XDecreasingSequence<E> extends XProcessingSequence<E>, XSettingSequence<E>
{
	public interface Creator<E> extends XProcessingSequence.Factory<E>, XSettingSequence.Creator<E>
	{
		@Override
		public XDecreasingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XDecreasingSequence<E> setAll(long index, E... elements);

	@Override
	public XDecreasingSequence<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XDecreasingSequence<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XDecreasingSequence<E> swap(long indexA, long indexB);

	@Override
	public XDecreasingSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XDecreasingSequence<E> copy();

	@Override
	public XDecreasingSequence<E> toReversed();

	@Override
	public XDecreasingSequence<E> reverse();

	@Override
	public XDecreasingSequence<E> range(long fromIndex, long toIndex);

	@Override
	public XDecreasingSequence<E> sort(Comparator<? super E> comparator);

}
