
package one.microstream.integrations.cdi.types.interceptor;

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

import org.junit.jupiter.api.Test;

import one.microstream.integrations.cdi.types.test.CDIExtension;

import javax.inject.Inject;


@CDIExtension
class StoreInterceptorTest
{
	@Inject
	private AgendaLazyService  lazyService;
	
	@Inject
	private AgendaEagerService eagerService;
	
	@Test
	public void shouldUpdateRootLazily()
	{
		this.lazyService.addNameRoot("Poliana");
	}
	
	@Test
	public void shouldUpdateFieldsLazily()
	{
		this.lazyService.addNameLazy("Otavio");
	}
	
	@Test
	public void shouldUpdateLazilyAField()
	{
		this.lazyService.updateName("Otavio");
	}
	
	@Test
	public void shouldUpdateRootEagerly()
	{
		this.eagerService.add("Ada");
	}
	
	@Test
	public void shouldUpdateFieldsEagerly()
	{
		this.eagerService.addEager("Ada");
	}
	
	@Test
	public void shouldUpdateEagerlyAFields()
	{
		this.lazyService.updateName("Otavio");
	}
	
}
