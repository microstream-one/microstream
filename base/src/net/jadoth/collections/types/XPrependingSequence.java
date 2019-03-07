package net.jadoth.collections.types;

import java.util.function.Consumer;

import net.jadoth.collections.interfaces.CapacityExtendable;
import net.jadoth.collections.interfaces.ExtendedSequence;
import net.jadoth.collections.interfaces.OptimizableCollection;

public interface XPrependingSequence<E>
extends Consumer<E>, CapacityExtendable, OptimizableCollection, ExtendedSequence<E>
{
	public interface Creator<E>
	{
		public XPrependingSequence<E> newInstance();
	}



	public boolean prepend(E element);

	public boolean nullPrepend();

	@SuppressWarnings("unchecked")
	public XPrependingSequence<E> prependAll(E... elements);

	public XPrependingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	public XPrependingSequence<E> prependAll(XGettingCollection<? extends E> elements);

}
