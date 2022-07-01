
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

import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;

import one.microstream.reference.LazyReferenceManager;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.types.StorageManager;


@ApplicationScoped
public class StorageManagerProducer
{
	private static final Logger LOGGER = Logger.getLogger(StorageManagerProducer.class.getName());
	
	@Inject
	private Config              config;
	
	@Produces
	@ApplicationScoped
	public StorageManager getStoreManager()
	{
		
		final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
		LOGGER.info(
			"Loading default StorageManager loading from MicroProfile Config properties. The keys: "
				+ properties.keySet()
		);
		
		final EmbeddedStorageConfigurationBuilder builder = EmbeddedStorageConfigurationBuilder.New();
		for(final Map.Entry<String, String> entry : properties.entrySet())
		{
			builder.set(entry.getKey(), entry.getValue());
		}
		return builder.createEmbeddedStorageFoundation().start();
	}
	
	public void dispose(@Disposes final StorageManager manager)
	{
		LOGGER.info("Closing the default StorageManager");
		manager.close();
	}
}
