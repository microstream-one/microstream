package one.microstream.collections.lazy;

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

import java.util.List;

import one.microstream.reference.Lazy;

/**
 * A {@link List}, which uses {@link Lazy} references internally,
 * to enable automatic partial loading of the list's content.
 *
 * @param <E> the type of elements in this collection
 */
public interface LazyList<E> extends List<E>, LazyCollection<E>
{
	// just a marker interface, for now
}
