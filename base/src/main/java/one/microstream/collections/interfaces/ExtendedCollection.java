package one.microstream.collections.interfaces;

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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Basic interface that contains all general procedures that are common to any type of extended collection.
 *
 * @param <E> type of contained elements
 */
public interface ExtendedCollection<E>
{
	/**
	 * Defines if null-elements are allowed inside the collection or not.
	 * @return {@code true} if null is allowed inside the collection; {@code false} if not
	 */
	public boolean nullAllowed();

	/**
	 * Tells if this collection contains volatile elements.<br>
	 * An element is volatile, if it can become no longer reachable by the collection without being removed from the
	 * collection. Examples are {@link WeakReference} of {@link SoftReference} or implementations of collection entries
	 * that remove the element contained in an entry by some means outside the collection.<br>
	 * Note that {@link WeakReference} instances that are added to a simple (non-volatile) implementation of a
	 * collection do <b>not</b> make the collection volatile, as the elements themselves (the reference instances) are still
	 * strongly referenced.
	 *
	 * @return {@code true} if the collection contains volatile elements.
	 */
	public boolean hasVolatileElements();


	public interface Creator<E, C extends ExtendedCollection<E>>
	{
		public C newInstance();
	}

}
