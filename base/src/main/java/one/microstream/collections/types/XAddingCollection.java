package one.microstream.collections.types;

import java.util.function.Consumer;

import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.ExtendedCollection;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.functional.Aggregator;


/**
 * Adding aspect:<br>
 * add all elements that do not logically conflict with already contained elements
 * according to the collection's logic. ("add to"/"increase" collection).
 * <p>
 * Examples:
 * Set: Only add element, if no equal element is already contained
 * Bag: Always add all elements
 *
 * 
 *
 */
public interface XAddingCollection<E>
extends ExtendedCollection<E>, CapacityExtendable, OptimizableCollection, Consumer<E>
{
	public interface Creator<E> extends XFactory<E>
	{
		@Override
		public XAddingCollection<E> newInstance();
	}


	public default Aggregator<E, ? extends XAddingCollection<E>> collector()
	{
		return new Aggregator<E, XAddingCollection<E>>()
		{
			@Override
			public void accept(final E element)
			{
				XAddingCollection.this.add(element);
			}

			@Override
			public XAddingCollection<E> yield()
			{
				return XAddingCollection.this;
			}
		};
	}


	@Override
	public default void accept(final E element)
	{
		this.add(element);
	}

	public boolean add(E element);

	public boolean nullAdd();

	@SuppressWarnings("unchecked")
	public XAddingCollection<E> addAll(E... elements);

	public XAddingCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	public XAddingCollection<E> addAll(XGettingCollection<? extends E> elements);

	/* (20.06.2012 TM)TODO: strictAdd(), etc?
	 * Maybe provide another set of collecting methods that throws an exception
	 * if the element cannot be added?
	 *
	 * Rationale:
	 * Would enable collections to be used in a way similar to SQL tables with constraints:
	 * - An algorithm collecting elements into a collection without an exception is guaranteed
	 *   to not produce any element conflicts
	 *
	 * - In other words: It would spare the repeated boiler-plate code
	 *   if(!collection.add(element))
{
	 *       throw new ...
	 *   }
	 *   and instead replace it with
	 *   collection.strictAdd(element);
	 *
	 * - It would not increase intellisense clutter when using normal add() variante because of the prefixed strict~
	 *
	 * Note:
	 * There would be no need for a corresponding strictPut() set of methods,
	 * because put is defined as guaranteed collection, replacing conflicted elements if needed.
	 * strictAdd() would be kind of an equivalent for add() what the alternative putGet() is to put().
	 *
	 * There would, however, be the consequent need for a strictInsert() set of methods (but again, no strictInput).
	 *
	 * What about strictRemove() methods (throw exception if the passed element could not be found) ?
	 * strictReplace()?
	 * :-/
	 */

}
