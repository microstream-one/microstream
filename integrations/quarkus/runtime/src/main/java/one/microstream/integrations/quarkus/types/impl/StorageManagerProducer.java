
package one.microstream.integrations.quarkus.types.impl;

/*-
 * #%L
 * MicroStream Quarkus Extension - Runtime
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

import one.microstream.integrations.quarkus.types.config.EmbeddedStorageFoundationCustomizer;
import one.microstream.integrations.quarkus.types.config.StorageManagerInitializer;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageManager;
import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Map;


@ApplicationScoped
public class StorageManagerProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageManagerProducer.class);

    @Inject
    Config config;

    @Inject
    StorageBean storageBean;


    @Inject
    Instance<EmbeddedStorageFoundationCustomizer> customizers;

    @Inject
    Instance<StorageManagerInitializer> initializers;

    @Produces
    @ApplicationScoped
    public StorageManager getStorageManager() {

        final Map<String, String> properties = ConfigurationCoreProperties.getProperties(this.config);
        LOGGER.info(
                "Loading default StorageManager from MicroProfile Config properties. The keys: "
                        + properties.keySet()
        );

        final EmbeddedStorageConfigurationBuilder builder = EmbeddedStorageConfigurationBuilder.New();
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            builder.set(entry.getKey(), entry.getValue());
        }
        final EmbeddedStorageFoundation<?> foundation = builder.createEmbeddedStorageFoundation();
        foundation.setDataBaseName("Generic");

        LOGGER.debug("Executing EmbeddedStorageFoundationCustomizer beans");

        this.customizers.stream()
                .forEach(customizer -> customizer.customize(foundation));

        // Required when using Quarkus
        foundation.onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(
                Thread.currentThread()
                        .getContextClassLoader())));


        final EmbeddedStorageManager storageManager = foundation
                .createEmbeddedStorageManager();

        if (this.isAutoStart(properties)) {
            LOGGER.debug("Start StorageManager");
            storageManager.start();
        }

        if (!this.storageBean.isDefined()) {
            LOGGER.debug("Executing StorageManagerInitializer beans");
            // Only execute at this point when no storage root bean has defined with @Storage
            // Initializers are called from StorageBeanCreator.create if user has defined @Storage.
            this.initializers.stream()
                    .forEach(initializer -> initializer.initialize(storageManager));
        }

        return storageManager;
    }


    private boolean isAutoStart(final Map<String, String> properties) {
        return Boolean.parseBoolean(properties.getOrDefault("autoStart", "true"));

    }

    public void dispose(@Disposes final StorageManager manager) {
        LOGGER.info("Closing the default StorageManager");
        manager.close();
    }
}
