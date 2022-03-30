package one.microstream.functional;

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

/**
 * Additionally to the element to accept, this class' {@link IndexedAcceptor#accept(Object, long)} method,
 * uses the coherent index of the given element.
 * 
 * @param <T> type of element to accept
 *
 */
public interface IndexedAcceptor<T>
{
	/**
	 * Expects the element and its coherent index.
	 * 
	 * @param e element which is expected at the given index
	 * @param index on which the element is expected
	 */
	public void accept(T e, long index);
}
