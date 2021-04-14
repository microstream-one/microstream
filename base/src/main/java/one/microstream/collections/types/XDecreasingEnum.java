package one.microstream.collections.types;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Intermediate list type that combines all list aspects except increasing (adding and inserting), effectively causing
 * instances of this list type to maintain its size or shrink, but never grow.
 * <p>
 * This type is primarily used for the values list of a map, which can offer all functionality except adding
 * values (without mapping it to a key).
 *
 * 
 */
public interface XDecreasingEnum<E> extends XProcessingEnum<E>, XSettingEnum<E>, XDecreasingSequence<E>, XReplacingCollection<E>
{
	public interface Creator<E> extends XProcessingEnum.Creator<E>, XSettingEnum.Creator<E>, XDecreasingSequence.Creator<E>
	{
		@Override
		public XDecreasingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XDecreasingEnum<E> setAll(long index, E... elements);

	@Override
	public XDecreasingEnum<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XDecreasingEnum<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XDecreasingEnum<E> swap(long indexA, long indexB);

	@Override
	public XDecreasingEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XDecreasingEnum<E> copy();

	@Override
	public XDecreasingEnum<E> toReversed();

	@Override
	public XDecreasingEnum<E> reverse();

	@Override
	public XDecreasingEnum<E> range(long fromIndex, long toIndex);

	@Override
	public XDecreasingEnum<E> sort(Comparator<? super E> comparator);
	
	@Override
	public long substitute(Function<? super E, ? extends E> mapper);

}
