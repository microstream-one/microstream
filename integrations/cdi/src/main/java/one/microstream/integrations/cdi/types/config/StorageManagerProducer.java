
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

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.microstream.integrations.cdi.types.ConfigurationCoreProperties;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageManager;


@ApplicationScoped
public class StorageManagerProducer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageManagerProducer.class);

	@Inject
	private Config config;

	@Inject
	private StorageExtension storageExtension;

	@Inject
	private Instance<EmbeddedStorageFoundationCustomizer> customizers;

	@Inject
	private Instance<StorageManagerInitializer> initializers;

	@Produces
	@ApplicationScoped
	public StorageManager getStorageManager()
	{

		if (this.storageExtension.getStorageManagerConfigInjectionNames()
				.isEmpty())
		{
			return this.storageManagerFromProperties();
		}

		// StorageManager through StorageManagerConverter
		final String configName = this.storageExtension.getStorageManagerConfigInjectionNames()
				.iterator()
				.next();
		LOGGER.info(
				"Loading StorageManager from file indicated by MicroProfile Config key : "
						+ configName
		);

		// This will succeed since it is already validated during deployment of the application.
		return this.config.getValue(configName, StorageManager.class);
	}

	private EmbeddedStorageManager storageManagerFromProperties()
	{
		final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
		LOGGER.info(
				"Loading default StorageManager from MicroProfile Config properties. The keys: "
						+ properties.keySet()
		);

		final EmbeddedStorageConfigurationBuilder builder = EmbeddedStorageConfigurationBuilder.New();
		for (final Map.Entry<String, String> entry : properties.entrySet())
		{
			builder.set(entry.getKey(), entry.getValue());
		}
		final EmbeddedStorageFoundation<?> foundation = builder.createEmbeddedStorageFoundation();
		foundation.setDataBaseName("Generic");

		this.customizers.stream()
				.forEach(customizer -> customizer.customize(foundation));

		final EmbeddedStorageManager storageManager = foundation
				.createEmbeddedStorageManager();

		if (this.isAutoStart(properties))
		{
			storageManager.start();
		}

		if (!this.storageExtension.hasStorageRoot())
		{
			// Only execute at this point when no storage root bean has defined with @Storage
			// Initializers are called from StorageBean.create if user has defined @Storage and root is read.
			this.initializers.stream()
					.forEach(initializer -> initializer.initialize(storageManager));
		}

		return storageManager;
	}

	private boolean isAutoStart(final Map<String, String> properties)
	{
		return Boolean.parseBoolean(properties.getOrDefault("autoStart", "true"));

	}

	public void dispose(@Disposes final StorageManager manager)
	{
		LOGGER.info("Closing the default StorageManager");
		manager.close();
	}
}
