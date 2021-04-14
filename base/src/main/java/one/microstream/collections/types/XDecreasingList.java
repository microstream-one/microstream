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
public interface XDecreasingList<E> extends XProcessingList<E>, XSettingList<E>, XDecreasingSequence<E>
{
	public interface Creator<E> extends XProcessingList.Factory<E>, XSettingList.Creator<E>, XDecreasingSequence.Creator<E>
	{
		@Override
		public XDecreasingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XDecreasingList<E> setAll(long index, E... elements);

	@Override
	public XDecreasingList<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XDecreasingList<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XDecreasingList<E> swap(long indexA, long indexB);

	@Override
	public XDecreasingList<E> swap(long indexA, long indexB, long length);

	@Override
	public XDecreasingList<E> copy();

	@Override
	public XDecreasingList<E> toReversed();

	@Override
	public XDecreasingList<E> reverse();

	@Override
	public XDecreasingList<E> range(long fromIndex, long toIndex);

	@Override
	public XDecreasingList<E> fill(long offset, long length, E element);

	@Override
	public XDecreasingList<E> sort(Comparator<? super E> comparator);
	
}
