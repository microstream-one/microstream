package one.microstream.integrations.spring.boot.types.config;

/*-
 * #%L
 * microstream-integrations-spring-boot
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

import one.microstream.integrations.spring.boot.types.storage.StorageClassData;
import one.microstream.integrations.spring.boot.types.storage.StorageMetaData;
import one.microstream.integrations.spring.boot.types.util.ByQualifier;
import one.microstream.integrations.spring.boot.types.util.EnvironmentFromMap;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.util.logging.Logging;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bean that can provide initialized instances of the MicroStream StorageManager.
 * When called programmatically, it creates a new StorageManager or retrieves the previously created one.
 * This class will also be used when using the Spring beans directly (with or without @Primary)
 */
@Component
public class StorageManagerProvider
{
    public static final String PRIMARY_QUALIFIER = "Primary";


    private final static Map<String, EmbeddedStorageManager> storageManagers = new ConcurrentHashMap<>();

    private static final String PREFIX = "one.microstream.";

    private final static Logger logger = Logging.getLogger(StorageManagerFactory.class);

    private final List<EmbeddedStorageFoundationCustomizer> customizers;
    private final List<StorageManagerInitializer> initializers;

    // StorageMetaData bean is created when @Storage is found. Impacts the execution of
    // StorageManagerInitializer's
    private final Optional<StorageMetaData> storageMetaData;
    private final Environment env;
    private final ApplicationContext applicationContext;

    public StorageManagerProvider(
            final List<EmbeddedStorageFoundationCustomizer> customizers,
            final List<StorageManagerInitializer> initializers,
            final Optional<StorageMetaData> storageMetaData,
            final Environment env,
            final ApplicationContext applicationContext)
    {
        this.customizers = customizers;
        this.initializers = initializers;
        this.storageMetaData = storageMetaData;
        this.env = env;
        this.applicationContext = applicationContext;
    }

    private Map<String, String> readProperties(final String qualifier)
    {
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

        final String qualifierPrefix = PREFIX + (PRIMARY_QUALIFIER.equalsIgnoreCase(qualifier) ? "" : qualifier + '.');

        return sources.stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .filter(prop -> (prop.startsWith(qualifierPrefix) && env.getProperty(prop) != null))
                .collect(Collectors.toMap(prop -> prop.replaceFirst(qualifierPrefix, ""), env::getProperty));
    }

    private Map<String, String> normalizeProperties(final Map<String, String> properties)
    {
        return properties.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        kv -> ConfigurationPropertyName.of(kv.getKey())
                                .toString(),
                        Map.Entry::getValue
                ));
    }

    public EmbeddedStorageManager get(final String qualifier)
    {
        return storageManagers.computeIfAbsent(qualifier, this::create);
    }

    private EmbeddedStorageManager create(final String qualifier)
    {

        final Map<String, String> values = this.normalizeProperties(this.readProperties(qualifier));

        final EmbeddedStorageFoundation<?> embeddedStorageFoundation = this.embeddedStorageFoundation(qualifier, values);

        final MicrostreamConfigurationProperties configuration = new MicrostreamConfigurationProperties();
        // Bind the map of config values to the instance of the @ConfigurationProperties annotated class
        // name (prefix) is blank as we stripped it already in readProperties
        Binder.get(new EnvironmentFromMap(values))
                .bind("", Bindable.ofInstance(configuration));

        if (configuration.getUseCurrentThreadClassLoader() != null && configuration.getUseCurrentThreadClassLoader())
        {
            embeddedStorageFoundation.onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(
                    Thread.currentThread()
                            .getContextClassLoader())));
        }
        embeddedStorageFoundation.onConnectionFoundation(
                cf -> cf.setClassLoaderProvider(typeName -> applicationContext.getClassLoader()));

        ByQualifier.filter(this.customizers, qualifier)
                .forEach(c -> c.customize(embeddedStorageFoundation));

        EmbeddedStorageManager storageManager = embeddedStorageFoundation.createEmbeddedStorageManager();

        if (configuration.getAutoStart() != null && configuration.getAutoStart())
        {
            storageManager.start();
        }


        if (!hasRootDefined(qualifier))
        {
            // No @Storage,so we need to execute initializers now.
            // Otherwise the StorageBeanFactory.createRootObject is responsible for calling the
            ByQualifier.filter(this.initializers, qualifier)
                    .forEach(i -> i.initialize(storageManager));
        }

        return storageManager;
    }

    private boolean hasRootDefined(final String qualifier)
    {
        if (storageMetaData.isEmpty())
        {
            // No @Storage at all -> so no root
            return false;
        }

        Optional<StorageClassData> storageClassData = this.storageMetaData.get()
                .getStorageClassData()
                .stream()
                .filter(scd -> scd.getQualifier()
                        .equals(qualifier))
                .findAny();
        return storageClassData.isPresent();
    }

    public EmbeddedStorageFoundation<?> embeddedStorageFoundation()
    {
        final Map<String, String> values = this.normalizeProperties(this.readProperties(PRIMARY_QUALIFIER));
        return embeddedStorageFoundation(PRIMARY_QUALIFIER, values);
    }

    private EmbeddedStorageFoundation<?> embeddedStorageFoundation(final String qualifier, final Map<String, String> values)
    {
        final EmbeddedStorageConfigurationBuilder builder = EmbeddedStorageConfigurationBuilder.New();

        if (values.containsKey("use-current-thread-class-loader"))
        {
            if (Objects.equals(values.get("use-current-thread-class-loader"), "true"))
            {
                logger.debug("using current thread class loader");
            }
            values.remove("use-current-thread-class-loader");

        }

        logger.debug("MicroStream configuration items: ");
        values.forEach((key, value) ->
                       {
                           if (value != null)
                           {
                               if (key.contains("password"))
                               {
                                   logger.debug(key + " : xxxxxx");
                               }
                               else
                               {
                                   logger.debug(key + " : " + value);
                               }
                               builder.set(key, value);
                           }
                       });


        EmbeddedStorageFoundation<?> storageFoundation = builder.createEmbeddedStorageFoundation();
        storageFoundation.setDataBaseName(qualifier);
        return storageFoundation;

    }
}
