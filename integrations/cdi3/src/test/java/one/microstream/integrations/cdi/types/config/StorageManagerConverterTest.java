
package one.microstream.integrations.cdi.types.config;

/*-
 * #%L
 * microstream-integrations-cdi3
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import io.smallrye.config.inject.ConfigExtension;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import one.microstream.storage.types.StorageManager;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;


@EnableAutoWeld
@AddExtensions(ConfigExtension.class)  // SmallRye Config extension to Support MicroProfile Config within this test
@AddExtensions(StorageExtension.class)
@DisplayName("Check if the Storage Manager will load using the default MicroProfile Properties file")
public class StorageManagerConverterTest extends AbstractStorageManagerConverterTest
{
	@Inject
	private StorageManager manager;

	@ApplicationScoped
	@Produces
	private StorageManager storageManagerMock = Mockito.mock(StorageManager.class);

	@Test
	public void shouldBeFromProducer()
	{
		Assertions.assertNotNull(this.manager);
		//Assertions.assertSame(this.storageManagerMock, this.manager);
		//Although it is the same instance, the types are different and thus assertSame fails
		Assertions.assertEquals(this.storageManagerMock.toString(), this.manager.toString());
	}
}
