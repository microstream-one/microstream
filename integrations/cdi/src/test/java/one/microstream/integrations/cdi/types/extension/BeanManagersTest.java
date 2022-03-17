
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import one.microstream.integrations.cdi.types.test.CDIExtension;


@CDIExtension
class BeanManagersTest
{
	@Inject
	private BeanManager beanManager;
	
	@Test
	public void shouldInstance()
	{
		final StorageExtension instance = BeanManagers.getInstance(StorageExtension.class, this.beanManager);
		assertNotNull(instance);
	}
	
	@Test
	public void shouldInstanceWithQualifier()
	{
		final StorageExtension instance =
			BeanManagers.getInstance(StorageExtension.class, new Default.Literal(), this.beanManager);
		assertNotNull(instance);
	}
	
	@Test
	public void shouldReturnBeanManager()
	{
		final BeanManager beanManager = BeanManagers.getBeanManager();
		assertNotNull(beanManager);
	}
	
	@Test
	public void shouldInstanceWithCurrentBeanManager()
	{
		final StorageExtension instance = BeanManagers.getInstance(StorageExtension.class);
		assertNotNull(instance);
	}
	
	@Test
	public void shouldInstanceWithQualifierWithCurrentBeanManager()
	{
		final StorageExtension instance = BeanManagers.getInstance(StorageExtension.class, new Default.Literal());
		assertNotNull(instance);
	}
	
}
