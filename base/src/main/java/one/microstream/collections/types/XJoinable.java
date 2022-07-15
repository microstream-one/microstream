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

import java.util.function.BiConsumer;

/**
 * 
 * @param <E> type of data to join
 */
public interface XJoinable<E>
{
	/**
	 * Iterates over all elements of the collections and calls the joiner
	 * with each element and the aggregate.
	 * 
	 * @param joiner is the actual function to do the joining
	 * @param aggregate where to join into
	 * @param <A> type of aggregate
	 * @return the joined aggregate
	 */
	public <A> A join(BiConsumer<? super E, ? super A> joiner, A aggregate);
}
