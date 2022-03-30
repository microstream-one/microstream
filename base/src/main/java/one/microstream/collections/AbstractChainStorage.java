package one.microstream.collections;

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
import java.util.function.Function;

import one.microstream.collections.interfaces.ChainStorage;


public abstract class AbstractChainStorage<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
implements ChainStorage<E, K, V, EN>
{
	protected abstract EN head();

	protected abstract void disjoinEntry(EN entry);

	protected abstract boolean moveToStart(EN entry);

	protected abstract boolean moveToEnd(EN entry);
	
	protected abstract void replace(EN doomedEntry, EN keptEntry);
	
	protected abstract long substitute(Function<? super E, ? extends E> mapper, BiConsumer<EN, E> callback);
}
