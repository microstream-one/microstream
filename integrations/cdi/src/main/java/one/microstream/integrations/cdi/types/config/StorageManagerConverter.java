
package one.microstream.integrations.cdi.types.config;

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

import one.microstream.integrations.cdi.types.ConfigurationCoreProperties;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageManager;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A Config converter to {@link StorageManager}
 */
public class StorageManagerConverter implements Converter<StorageManager>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageManagerConverter.class);

	private static final Map<String, StorageManager> MAP = new ConcurrentHashMap<>();

	@Override
	public StorageManager convert(final String value) throws IllegalArgumentException, NullPointerException
	{
		return MAP.computeIfAbsent(value, this::createStorageManager);
	}

	private StorageManager createStorageManager(final String value)
	{
		LOGGER.info("Loading configuration to start the class StorageManager from the key: " + value);
		EmbeddedStorageFoundation<?> foundation = EmbeddedStorageConfiguration.load(value)
				.createEmbeddedStorageFoundation();

		CDI.current()
				.select(EmbeddedStorageFoundationCustomizer.class)
				.stream()
				.forEach(customizer -> customizer.customize(foundation));

		EmbeddedStorageManager storageManager = foundation
				.createEmbeddedStorageManager();

		if (isAutoStart())
		{
			storageManager.start();
		}

		CDI.current()
				.select(StorageManagerInitializer.class)
				.stream()
				.forEach(initializer -> initializer.initialize(storageManager));


		return storageManager;
	}

	private boolean isAutoStart()
	{
		return ConfigProvider.getConfig()
				.getOptionalValue(ConfigurationCoreProperties.Constants.PREFIX + "autoStart", Boolean.class)
				.orElse(Boolean.TRUE);
	}

}
