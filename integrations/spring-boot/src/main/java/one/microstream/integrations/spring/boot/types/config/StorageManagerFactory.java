package one.microstream.integrations.spring.boot.types.config;

/*-
 * #%L
 * microstream-spring
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

import one.microstream.integrations.spring.boot.types.storage.StorageMetaData;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.util.logging.Logging;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Configuration
public class StorageManagerFactory
{

    private static final String PREFIX = "one.microstream.";
    private final static Logger logger = Logging.getLogger(StorageManagerFactory.class);

    private final List<EmbeddedStorageFoundationCustomizer> customizers;
    private final List<StorageManagerInitializer> initializers;

    // StorageMetaData bean is created when @Storage is found. Impacts the execution of
    // StorageManagerInitializer's
    private final Optional<StorageMetaData> storageMetaData;

    public StorageManagerFactory(
            final List<EmbeddedStorageFoundationCustomizer> customizers
            , final List<StorageManagerInitializer> initializers
            , final Optional<StorageMetaData> storageMetaDataOptional
    )
    {
        this.customizers = customizers;
        this.initializers = initializers;
        this.storageMetaData = storageMetaDataOptional;
    }

    public Map<String, String> readProperties(final Environment env)
    {
        final Map<String, String> rtn = new HashMap<>();

        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

        return sources.stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .filter(prop -> (prop.contains(PREFIX) && env.getProperty(prop) != null))
                .collect(Collectors.toMap(prop -> prop.replaceFirst(PREFIX, ""), env::getProperty));
    }

    public Map<String, String> normalizeProperties(final Map<String, String> properties)
    {
        return properties.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        kv -> ConfigurationPropertyName.of(kv.getKey())
                                .toString(),
                        Map.Entry::getValue
                ));
    }

    @Bean
    @Lazy
    public EmbeddedStorageFoundation<?> embeddedStorageFoundation(final Environment env)
    {
        final Map<String, String> values = this.normalizeProperties(this.readProperties(env));

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


        return builder.createEmbeddedStorageFoundation();

    }

    @Bean(destroyMethod = "shutdown")
    @Lazy
    public EmbeddedStorageManager embeddedStorageManager(final Environment env, final MicrostreamConfigurationProperties configuration)
    {
        final EmbeddedStorageFoundation<?> embeddedStorageFoundation = this.embeddedStorageFoundation(env);

        if (configuration.getUseCurrentThreadClassLoader() != null && configuration.getUseCurrentThreadClassLoader())
        {
            embeddedStorageFoundation.onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(
                    Thread.currentThread()
                            .getContextClassLoader())));
        }

        customizers.forEach(c -> c.customize(embeddedStorageFoundation));

        EmbeddedStorageManager storageManager = embeddedStorageFoundation.createEmbeddedStorageManager();

        if (configuration.getAutoStart() != null && configuration.getAutoStart())
        {
            storageManager.start();
        }


        if (!storageMetaData.isPresent()) {
            // No @Storage,so we need to execute initializers now.
            // Otherwise the StorageBeanFactory.createRootObject is responsible for calling the
            initializers.forEach(i -> i.initialize(storageManager));
        }

        return storageManager;
    }

}
