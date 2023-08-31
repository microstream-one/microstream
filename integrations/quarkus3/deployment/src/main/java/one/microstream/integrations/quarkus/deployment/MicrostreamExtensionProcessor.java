package one.microstream.integrations.quarkus.deployment;

/*-
 * #%L
 * MicroStream Quarkus 3 Extension - Deployment
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


import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import one.microstream.integrations.quarkus.types.Storage;
import one.microstream.integrations.quarkus.types.impl.BeanCreatorParameterNames;
import one.microstream.integrations.quarkus.types.impl.RootCreator;
import one.microstream.integrations.quarkus.types.impl.StorageBean;
import one.microstream.integrations.quarkus.types.impl.StorageBeanCreator;
import one.microstream.integrations.quarkus.types.impl.StorageClassInfo;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quarkus extension processor to handle the {@link Storage} annotation.
 */
class MicrostreamExtensionProcessor
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrostreamExtensionProcessor.class);

    private static final DotName STORAGE_ANNOTATION = DotName.createSimple(Storage.class.getName());

    private static final DotName INJECT_ANNOTATION = DotName.createSimple(Inject.class.getName());

    private static final String FEATURE = "microstream-extension";

    @BuildStep
    FeatureBuildItem feature()
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    StorageBeanBuildItem findStorageRoot(final BeanArchiveIndexBuildItem beanArchiveIndex)
    {
        LOGGER.debug("BuildStep for MicroStream: Find any class that is annotated with @Storage");
        final Set<StorageClassInfo> rootClasses = findAnnotatedClasses(beanArchiveIndex);
        if (rootClasses.size() > 1)
        {
            throw new IllegalStateException(
                    "In the application you must have only one class with the Storage annotation, classes: "
                            + rootClasses);
        }
        return new StorageBeanBuildItem(rootClasses.stream()
                                                .findAny()
                                                .orElse(null));
    }

    @BuildStep
    SyntheticBeanBuildItem rootBean(final StorageBeanBuildItem storageBeanBuildItem)
    {

        final Optional<Class<?>> rootClass = storageBeanBuildItem.getRootClass();

        if (rootClass.isPresent())
        {
            LOGGER.debug("BuildStep for MicroStream: Configure a SyntheticBeanBuildItem for the @Storage bean");
            LOGGER.info(String.format("Processing Extension: @Storage found at %s", rootClass));
            return SyntheticBeanBuildItem.configure(rootClass.get())
                    .scope(Singleton.class)
                    .creator(RootCreator.class)
                    .done();

        }
        else
        {
            // Quarkus is fine with returning null for a BuildItem instance.
            return null;

        }

    }

    @BuildStep
    SyntheticBeanBuildItem storageBean(final StorageBeanBuildItem storageBeanBuildItem)
    {
        LOGGER.debug("BuildStep for MicroStream: Configure a SyntheticBeanBuildItem for the bean keeping info about @Storage class");

        final Optional<Class<?>> rootClass = storageBeanBuildItem.getRootClass();

        final String fields = String.join(",", storageBeanBuildItem.getFieldsToInject());

        // .orElse is needed due to https://github.com/quarkusio/quarkus/issues/27664
        return SyntheticBeanBuildItem.configure(StorageBean.class)
                .scope(Singleton.class)
                .creator(StorageBeanCreator.class)
                .param(BeanCreatorParameterNames.CLASS_NAME, rootClass.orElse(Object.class))
                .param(BeanCreatorParameterNames.FIELDS, fields)
                .done();

    }



    private Set<StorageClassInfo> findAnnotatedClasses(final BeanArchiveIndexBuildItem beanArchiveIndex)
    {
        final Set<StorageClassInfo> result = new HashSet<>();
        final IndexView indexView = beanArchiveIndex.getIndex();
        final Collection<AnnotationInstance> storageBeans = indexView.getAnnotations(STORAGE_ANNOTATION);

        for (AnnotationInstance ann : storageBeans)
        {
            ClassInfo beanClassInfo = ann.target()
                    .asClass();

            result.add(createStorageBeanClassInfo(beanClassInfo));

        }
        return result;
    }

    private StorageClassInfo createStorageBeanClassInfo(final ClassInfo beanClassInfo)
    {
        try
        {
            Class<?> classReference = Class.forName(beanClassInfo.name()
                                                         .toString(), false, Thread.currentThread()
                                                         .getContextClassLoader());

            List<String> fieldsToInject = beanClassInfo.fields()
                    .stream()
                    .filter(fi -> fi.hasAnnotation(INJECT_ANNOTATION))
                    .map(FieldInfo::name)
                    .collect(Collectors.toList());

            return new StorageClassInfo(classReference, fieldsToInject);

        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Unable to load Class ", e);
        }
    }

}
