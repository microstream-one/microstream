package one.microstream.collections.types;

import java.util.Comparator;

public interface XEnum<E> extends XBasicEnum<E>, XSequence<E>, XIncreasingEnum<E>, XDecreasingEnum<E>
{
	public interface Creator<E> extends XBasicEnum.Creator<E>, XSequence.Creator<E>
	{
		@Override
		public XEnum<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XEnum<E> copy();

	@Override
	public XEnum<E> toReversed();

	@Override
	public XEnum<E> reverse();

	@Override
	public XEnum<E> sort(Comparator<? super E> comparator);

	@Override
	public XEnum<E> range(final long lowIndex, final long highIndex);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> addAll(E... elements);

	@Override
	public XEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> putAll(E... elements);

	@Override
	public XEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> prependAll(E... elements);

	@Override
	public XEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> preputAll(E... elements);

	@Override
	public XEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> preputAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> setAll(long index, E... elements);

	@Override
	public XEnum<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XEnum<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XEnum<E> swap(long indexA, long indexB);

	@Override
	public XEnum<E> swap(long indexA, long indexB, long length);

}
