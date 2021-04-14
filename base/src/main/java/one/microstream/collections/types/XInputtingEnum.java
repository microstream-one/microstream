package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XInputtingEnum<E> extends XInsertingEnum<E>, XExpandingEnum<E>
{
	public interface Creator<E> extends XInsertingEnum.Creator<E>, XExpandingEnum.Creator<E>
	{
		@Override
		public XInputtingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> addAll(E... elements);

	@Override
	public XInputtingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> putAll(E... elements);

	@Override
	public XInputtingEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> prependAll(E... elements);

	@Override
	public XInputtingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> preputAll(E... elements);

	@Override
	public XInputtingEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> preputAll(XGettingCollection<? extends E> elements);

}
