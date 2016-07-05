package net.jadoth.collections.types;

import net.jadoth.collections.interfaces.ReleasingCollection;


public interface XPreputtingEnum<E> extends XPreputtingSequence<E>, ReleasingCollection<E>
{
	public interface Creator<E> extends XPreputtingSequence.Creator<E>
	{
		@Override
		public XPreputtingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingEnum<E> prependAll(E... elements);

	@Override
	public XPreputtingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPreputtingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingEnum<E> preputAll(E... elements);

	@Override
	public XPreputtingEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPreputtingEnum<E> preputAll(XGettingCollection<? extends E> elements);

}
