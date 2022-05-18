
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

import static one.microstream.integrations.cdi.types.ConfigurationCoreProperties.CUSTOM;
import static one.microstream.integrations.cdi.types.ConfigurationCoreProperties.STORAGE_DIRECTORY;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import one.microstream.integrations.cdi.types.test.CDIExtension;


@CDIExtension
class ConfigurationCorePropertiesTest
{
	@Inject
	private Config config;
	
	@Test
	public void shouldCreateEmptyMap()
	{
		final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
		Assertions.assertNotNull(properties);
		Assertions.assertTrue(properties.isEmpty());
	}
	
	@Test
	public void shouldLoadPropertiesFromConfiguration()
	{
		System.setProperty(STORAGE_DIRECTORY.getMicroprofile(), "target/");
		final Map<String, String> properties       = ConfigurationCoreProperties.getProperties(this.config);
		final String              storageDirectory = properties.get(STORAGE_DIRECTORY.getMicrostream());
		Assertions.assertNotNull(storageDirectory);
		Assertions.assertEquals(storageDirectory, "target/");
		System.clearProperty(STORAGE_DIRECTORY.getMicroprofile());
	}
	
	@Test
	public void shouldAddCustomConfiguration()
	{
		final String customProperty = "custom.test";
		System.setProperty(CUSTOM.getMicroprofile() + "." + customProperty, "random_value");
		final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
		final String              value      = properties.get(customProperty);
		Assertions.assertEquals(value, "random_value");
		System.clearProperty(CUSTOM.getMicroprofile() + "." + customProperty);
	}
	
}
