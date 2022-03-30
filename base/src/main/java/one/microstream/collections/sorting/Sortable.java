package one.microstream.collections.sorting;

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

import java.util.Comparator;

/**
 * Single concern type defining that a sub type can be sorted according to an external {@link Comparator}.
 * <p>
 * This type is mutually exclusive to {@link Sorted}.
 *
 * @param <E> the type of the input to the operation
 *
 */
public interface Sortable<E>
{
	/**
	 * Sorts this collection according to the given comparator
	 * and returns itself.
	 * @param comparator to sort this collection
	 * @return this
	 */
	public Sortable<E> sort(Comparator<? super E> comparator);
}
