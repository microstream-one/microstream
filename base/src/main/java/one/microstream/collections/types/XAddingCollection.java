package one.microstream.collections.types;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
 * Examples:<br>
 * Set: Only add element, if no equal element is already contained<br>
 * Bag: Always add all elements
 *
 * @param <E> type of contained elements
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

	/**
	 * Adds the passed element.
	 * @param element to add
	 * @return {@code true} if element was added; {@code false} if not
	 */
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
	 */

}
