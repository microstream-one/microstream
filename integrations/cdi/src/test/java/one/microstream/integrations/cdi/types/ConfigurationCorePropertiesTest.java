
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

import io.smallrye.config.inject.ConfigExtension;
import one.microstream.integrations.cdi.types.logging.TestLogger;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationPropertyNames;
import org.eclipse.microprofile.config.Config;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@EnableAutoWeld  // So that Weld container is started
@AddExtensions(ConfigExtension.class)  // SmallRye Config extension to Support MicroProfile Config within this test
class ConfigurationCorePropertiesTest
{
	// Testing the ConfigurationCoreProperties functionality.
	// - convert the MicroProfile config key/values to Map entries as MicroStream config values.

	@Inject
	private Config config;

	@BeforeEach
	public void setup()
	{
		TestLogger.reset();
	}

	@Test
	void shouldLoadFromPropertiesFile()
	{
		final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
		Assertions.assertNotNull(properties);
		Assertions.assertEquals(4, properties.keySet()
				.size());
		Assertions.assertEquals(Set.of("xml", "ini", "properties", "storage-directory"), properties.keySet());
	}

	@Test
	void shouldLoadPropertiesFromConfiguration()
	{
		String microProfileKey = ConfigurationCoreProperties.STORAGE_DIRECTORY.getMicroProfile();
		try
		{
			System.setProperty(microProfileKey, "target/");
			final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
			final String storageDirectory = properties.get(ConfigurationCoreProperties.STORAGE_DIRECTORY.getMicroStream(microProfileKey));
			Assertions.assertNotNull(storageDirectory);
			Assertions.assertEquals("target/", storageDirectory);
		} finally
		{
			System.clearProperty(microProfileKey);
		}
	}
	
	@Test
	public void shouldAddCustomConfiguration()
	{

		final String customProperty = "custom.test";
		final String key = ConfigurationCoreProperties.Constants.PREFIX + customProperty;
		try
		{
			System.setProperty(key, "random_value");
			final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
			final String value = properties.get(customProperty);
			Assertions.assertEquals("random_value", value);
		} finally
		{
			System.clearProperty(key);
		}
	}

	@Test
	void shouldMapStorageFileSystem()
	{
		final String keyMicroProfile = "one.microstream.storage.filesystem.sql.postgres.data-source-provider";
		final String keyMicroStream = "storage-filesystem.sql.postgres.data-source-provider";
		try
		{
			System.setProperty(keyMicroProfile, "some_value");
			final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
			final String value = properties.get(keyMicroStream);
			Assertions.assertEquals("some_value", value);

		} finally
		{
			System.clearProperty(keyMicroProfile);
		}

	}

	@Test
	void shouldSupportMicroStreamKeys()
	{
		final String keyMicroProfile = "one.microstream.storage-directory";
		try
		{
			System.setProperty(keyMicroProfile, "/storage");
			final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
			final String value = properties.get(EmbeddedStorageConfigurationPropertyNames.STORAGE_DIRECTORY);
			Assertions.assertEquals("/storage", value);

		} finally
		{
			System.clearProperty(keyMicroProfile);
		}

	}

	@Test
	void shouldMapStorageFileSystem_directly()
	{
		// Without CDI version, testing ConfigurationCoreProperties 'directly'

		final String keyMicroProfile = "one.microstream.storage.filesystem.sql.postgres.data-source-provider";
		final String keyMicroStream = "storage-filesystem.sql.postgres.data-source-provider";

		Optional<ConfigurationCoreProperties> property = ConfigurationCoreProperties.get(keyMicroProfile);
		Assertions.assertTrue(property.isPresent());

		String convertedKey = property.get().getMicroStream(keyMicroProfile);
		Assertions.assertEquals(keyMicroStream, convertedKey);
	}

	@Test
	void shouldMapExactMatches()
	{
		// Not using CDI

		final String keyMicroProfile = "one.microstream.storage.directory";
		final String keyMicroStream = "storage-directory";

		Optional<ConfigurationCoreProperties> property = ConfigurationCoreProperties.get(keyMicroProfile);
		Assertions.assertTrue(property.isPresent());

		String convertedKey = property.get().getMicroStream(keyMicroProfile);
		Assertions.assertEquals(keyMicroStream, convertedKey);
	}

	@Test
	void findEnumValue()
	{
		// Not using CDI
		final Optional<ConfigurationCoreProperties> property = ConfigurationCoreProperties.get("one.microstream.storage.directory");
		Assertions.assertTrue(property.isPresent());
		Assertions.assertEquals(ConfigurationCoreProperties.STORAGE_DIRECTORY,  property.get());
	}

	@Test
	void findEnumValue_partial()
	{
		// Not using CDI
		String key = "one.microstream.storage.filesystem.sql.postgres.data-source-provider";
		final Optional<ConfigurationCoreProperties> property = ConfigurationCoreProperties.get(key);
		Assertions.assertTrue(property.isPresent());
		Assertions.assertEquals(ConfigurationCoreProperties.STORAGE_FILESYSTEM,  property.get());
		Assertions.assertEquals("storage-filesystem.sql.postgres.data-source-provider", property.get().getMicroStream(key));
	}

	@Test
	void findEnumValue_NotCaseSensitive()
	{
		// Not using CDI
		final Optional<ConfigurationCoreProperties> property = ConfigurationCoreProperties.get("one.microstream.Storage.Directory");
		Assertions.assertTrue(property.isEmpty());
	}
}
