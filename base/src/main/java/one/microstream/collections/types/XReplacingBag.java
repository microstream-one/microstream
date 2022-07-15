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

import java.util.function.Function;
import java.util.function.Predicate;


public interface XReplacingBag<E> extends XGettingCollection<E>, XReplacingCollection<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XReplacingBag<E> newInstance();
	}

	/**
	 * Replaces the first element that is equal to the given element
	 * with the replacement and then returns true.
	 * 
	 * @param element to replace
	 * @param replacement for the found element
	 * @return {@code true} if element is found, {@code false} if not
	 */
	public boolean replaceOne(E element, E replacement);

	public long replace(E element, E replacement);

	public long replaceAll(XGettingCollection<? extends E> elements, E replacement);

	public boolean replaceOne(Predicate<? super E> predicate, E replacement);
	
	public long replace(Predicate<? super E> predicate, E replacement);
		
	public long substitute(Predicate<? super E> predicate, Function<E, E> mapper);

	@Override
	public XReplacingBag<E> copy();
}
