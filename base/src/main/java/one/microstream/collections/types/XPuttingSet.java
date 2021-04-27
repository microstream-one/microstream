package one.microstream.collections.types;

import one.microstream.collections.interfaces.ReleasingCollection;


public interface XPuttingSet<E> extends XPuttingCollection<E>, XAddingSet<E>, ReleasingCollection<E>
{
	public interface Creator<E> extends XPuttingCollection.Creator<E>, XAddingSet.Creator<E>
	{
		@Override
		public XPuttingSet<E> newInstance();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it overwrites equal, already contained elements.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSet<E> putAll(E... elements);

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it overwrites equal, already contained elements.
	 */
	@Override
	public XPuttingSet<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it overwrites equal, already contained elements.
	 */
	@Override
	public XPuttingSet<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSet<E> addAll(E... elements);

	@Override
	public XPuttingSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingSet<E> addAll(XGettingCollection<? extends E> elements);

}
