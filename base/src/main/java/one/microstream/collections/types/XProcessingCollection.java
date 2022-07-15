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
import java.util.function.Predicate;

import one.microstream.equality.Equalator;
import one.microstream.functional.Processable;

/**
 * Curiously, a removing collection has to be a getting collection as well, because some removal procedures
 * could be abused to read the contained elements (e.g. {@link #retrieveBy(Predicate)}).
 * Splitting it up into a (pure) RemovingCollection and a RemGetCollection would cause
 * more structural clutter than it's worth.
 *
 * 
 */
public interface XProcessingCollection<E>
extends
Processable<E>,
XRemovingCollection<E>,
XGettingCollection<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XProcessingCollection<E> newInstance();
	}
	

	public E fetch(); // remove and retrieve first or throw IndexOutOfBoundsException if empty (fetch ~= first)

	public E pinch(); // remove and retrieve first or null if empty (like forcefull extraction from collection's base)

	public E retrieve(E element); // remove and retrieve first occurance

	public E retrieveBy(Predicate<? super E> predicate); // remove and retrieve first equal

	public long removeDuplicates(Equalator<? super E> equalator);

	public long removeBy(Predicate<? super E> predicate);

	public <C extends Consumer<? super E>> C moveTo(C target, Predicate<? super E> predicate);

	// getting override procedures //

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

	/*(01.12.2011 TM)XXX: processor() wrapper analog to view() wrapper.
	 * Required for "anything but bring in new element".
	 * E.g. if all kinds of adding/inserting etc. require a hook-in logic to be called but for everything else
	 * the collection (or its wrapper) can be used directly without having to delegate everything.
	 * Problem: What about cases where processing + ordering is desired? :-/
	 *
	 */
}
