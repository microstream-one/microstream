
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import one.microstream.integrations.cdi.types.Store;
import one.microstream.integrations.cdi.types.StoreType;


class StoreStrategyTest
{
	@Test
	public void shouldReturnNPE()
	{
		Assertions.assertThrows(NullPointerException.class, () -> StoreStrategy.of(null));
	}
	
	@Test
	public void shouldReturnEager()
	{
		final Store store = Mockito.mock(Store.class);
		Mockito.when(store.value()).thenReturn(StoreType.EAGER);
		final StoreStrategy strategy = StoreStrategy.of(store);
		Assertions.assertEquals(StoreTypeStrategy.EAGER, strategy);
	}
	
	@Test
	public void shouldReturnLazy()
	{
		final Store store = Mockito.mock(Store.class);
		Mockito.when(store.value()).thenReturn(StoreType.LAZY);
		final StoreStrategy strategy = StoreStrategy.of(store);
		Assertions.assertEquals(StoreTypeStrategy.LAZY, strategy);
	}
}
