
package one.microstream.integrations.cdi.types;

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

import one.microstream.integrations.cdi.types.extension.StorageExtension;
import one.microstream.storage.types.StorageManager;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.Set;


@EnableAutoWeld
@AddExtensions(StorageExtension.class)
public class StorageTest
{
	// Test if a class annotated with @Storage is converted into an ApplicationScoped bean.
	@Inject
	private Agenda agenda;

	@Inject
	private BeanManager beanManager;

	@ApplicationScoped
	@Produces
	// StorageBean requires a StorageManager
	private StorageManager storageManagerMock = Mockito.mock(StorageManager.class);

	@Test
	@DisplayName("Should check if it create an instance by annotation")
	public void shouldCreateInstance()
	{
		Assertions.assertNotNull(this.agenda);
		this.agenda.add("JUnit");

		// Another way of testing we have only 1 instance of @Storage bean.
		final Agenda instance = CDI.current()
				.select(Agenda.class)
				.get();
		Assertions.assertEquals("JUnit", instance.getNames()
				.iterator()
				.next());
	}

	@Test
	public void shouldCreateApplicationScopedBean()
	{
		final Set<Bean<?>> beans = this.beanManager.getBeans(Agenda.class);
		Assertions.assertEquals(1, beans.size());
		final Bean<?> storageBean = beans.iterator()
				.next();
		Assertions.assertEquals(ApplicationScoped.class, storageBean.getScope());

	}
}
