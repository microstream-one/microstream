package one.microstream.integrations.spring.boot.types.storage;

/*-
 * #%L
 * microstream-integrations-spring-boot-common
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

import one.microstream.integrations.spring.boot.types.MultipleStorageBeanException;
import one.microstream.integrations.spring.boot.types.Storage;
import one.microstream.integrations.spring.boot.types.config.StorageManagerInitializer;
import one.microstream.integrations.spring.boot.types.config.StorageManagerProvider;
import one.microstream.integrations.spring.boot.types.util.ByQualifier;
import one.microstream.reflect.XReflect;
import one.microstream.storage.types.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StorageBeanFactory implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor
{

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBeanFactory.class);

    private ApplicationContext applicationContext;

    private final List<StorageClassData> storageClasses = new ArrayList<>();

    public Object createRootObject(final String qualifier)
    {
        LOGGER.debug("Creating Spring bean for @Storage annotated class");

        final StorageManager storageManager;
        if (StorageManagerProvider.PRIMARY_QUALIFIER.equals(qualifier))
        {
            // Primary/single manager -> Lookup without qualifier
            storageManager = this.applicationContext.getBean(StorageManager.class);
        }
        else
        {
            storageManager = BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.applicationContext.getAutowireCapableBeanFactory()
                    , StorageManager.class, qualifier);

        }


        //final StorageManager storageManager = this.applicationContext.getBean(StorageManager.class);
        Object root = storageManager.root();

        if (Objects.isNull(root))
        {
            root = XReflect.defaultInstantiate(findRootClass(qualifier));
            LOGGER.debug(String.format("Created Root object from class %s", root.getClass()
                    .getName()));
            storageManager.setRoot(root);
            storageManager.storeRoot();
        }

        this.applicationContext
                .getAutowireCapableBeanFactory()
                .autowireBean(root);

        this.processInitializers(storageManager, qualifier);

        return root;
    }

    private Class findRootClass(final String qualifier)
    {

        return storageClasses.stream()
                .filter(scd -> scd.getQualifier()
                        .equals(qualifier))
                .findAny()
                .map(StorageClassData::getClazz)
                .orElseThrow(() ->
                             {
                                 // Should not happen

                                 throw new IllegalArgumentException("No Root definition found for Qualifier " + qualifier);
                             }
                );
    }

    private void processInitializers(final StorageManager storageManager, final String qualifier)
    {
        final String[] namesForType = applicationContext.getBeanNamesForType(StorageManagerInitializer.class);
        Arrays.stream(namesForType)
                .map(name -> applicationContext.getBean(name))
                .map(StorageManagerInitializer.class::cast)
                .filter(it -> ByQualifier.hasQualifierValue(it, qualifier))
                .forEach(initializer -> initializer.initialize(storageManager));

    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry beanDefinitionRegistry) throws
            BeansException
    {
        for (String definitionName : beanDefinitionRegistry.getBeanDefinitionNames())
        {

            final BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(definitionName);
            if (beanDefinition.getBeanClassName() != null)
            {
                try
                {
                    // Don't use generics as that will not work with the Supplier that provides the Root object further in this method
                    final Class clazz = Class.forName(beanDefinition.getBeanClassName());
                    final Storage storageAnnotation = (Storage) clazz.getAnnotation(Storage.class);
                    if (storageAnnotation != null)
                    {
                        this.storageClasses.add(new StorageClassData(clazz));

                        // Remove the original definition
                        beanDefinitionRegistry.removeBeanDefinition(definitionName);

                    }
                } catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }

        }
        final Map<String, List<Class>> storageClassByQualifier = storageClasses.stream()
                .collect(Collectors.groupingBy(StorageClassData::getQualifier
                        , Collectors.mapping(StorageClassData::getClazz, Collectors.toList())));

        final Map<String, List<Class>> multipleStorageClassByQualifier =
                storageClassByQualifier.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue()
                                .size() > 1)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (multipleStorageClassByQualifier.size() > 1)
        {
            throw new MultipleStorageBeanException(multipleStorageClassByQualifier);
        }
        if (!this.storageClasses.isEmpty())
        {
            // This is a marker bean used in the StorageManagerFactory
            beanDefinitionRegistry.registerBeanDefinition("storageMetaData", createStorageMetaDataDefinition());

            // Add a definition for each class annotated with @Storage
            // Add a new definition based on the createRootObject 'factory method'
            for (final StorageClassData storageClass : this.storageClasses)
            {

                beanDefinitionRegistry
                        .registerBeanDefinition(defineBeanName(storageClass),
                                                createBeanDefinition(storageClass)
                        );
            }
        }

    }

    private static String defineBeanName(StorageClassData storageClass)
    {

        // Bean name must be the qualifier name!
        String result = storageClass.getQualifier();
        if (StorageManagerProvider.PRIMARY_QUALIFIER.equals(result))
        {
            // Unless no Qualifier, than it must be the class name
            result = storageClass.getClazz()
                    .getName();
        }
        return result;
    }

    private BeanDefinition createBeanDefinition(final StorageClassData storageClass)
    {
        return BeanDefinitionBuilder.genericBeanDefinition(storageClass.getClazz(),
                                                           () -> createRootObject(storageClass.getQualifier()))
                //.addConstructorArgValue(storageClass.getQualifier())
                .getBeanDefinition();
    }

    private BeanDefinition createStorageMetaDataDefinition()
    {
        return BeanDefinitionBuilder.genericBeanDefinition(StorageMetaData.class,
                                                           () -> new StorageMetaData(storageClasses))
                .getBeanDefinition();
    }


    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory) throws
            BeansException
    {
        // nop-op
    }
}
