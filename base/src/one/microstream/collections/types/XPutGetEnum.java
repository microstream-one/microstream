package one.microstream.collections.types;


public interface XPutGetEnum<E> extends XGettingEnum<E>, XPuttingEnum<E>, XPutGetSet<E>, XPutGetSequence<E>
{
	public interface Creator<E> extends XGettingEnum.Creator<E>, XPuttingEnum.Creator<E>
	{
		@Override
		public XPutGetEnum<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetEnum<E> putAll(E... elements);

	@Override
	public XPutGetEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetEnum<E> addAll(E... elements);

	@Override
	public XPutGetEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetEnum<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XPutGetEnum<E> copy();

	@Override
	public XPutGetEnum<E> toReversed();

}
