package one.microstream.collections.types;

import java.util.function.Consumer;

import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.ExtendedSequence;
import one.microstream.collections.interfaces.OptimizableCollection;

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
