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

import one.microstream.collections.interfaces.ExtendedSequence;


/**
 * Single concern type defining that a sub type is always sorted according to an internal {@link Comparator}.
 * <p>
 * This definition extends the definition of being ordered.
 * <p>
 * This type is mutually exclusive to {@link Sortable}.
 *
 * @param <E> the type of the input to the operation
 */
public interface Sorted<E> extends ExtendedSequence<E>
{
	/**
	 *
	 * @return the {@link Comparator} that defines the sorting order of this {@link Sorted} instance.
	 */
	public Comparator<? super E> getComparator();
}
