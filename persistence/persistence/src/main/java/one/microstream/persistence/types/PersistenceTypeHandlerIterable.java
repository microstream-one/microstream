package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

public interface PersistenceTypeHandlerIterable<D>
{
	public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(C iterator);
	
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(C iterator);
	
	public default <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		this.iterateTypeHandlers(iterator);
		this.iterateLegacyTypeHandlers(iterator);
		
		return iterator;
	}
}
