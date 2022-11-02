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

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.BeanCreator;
import one.microstream.integrations.quarkus.types.config.StorageManagerInitializer;
import one.microstream.reflect.XReflect;
import one.microstream.storage.types.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RootCreator implements BeanCreator<Object>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RootCreator.class);

    @Override
    public Object create(final CreationalContext creationalContext, final Map map)
    {
        LOGGER.debug("Create Bean: Creating bean for MicroStream Root (@Storage annotated class)");

        final ArcContainer container = Arc.container();

        final StorageBean storageBean = container.instance(StorageBean.class)
                .get();

        LOGGER.info(String.format("Creation of the Root Bean from %s", storageBean.getInfo()
                .getClassReference()));

        final StorageManager storageManager = container
                .instance(StorageManager.class)
                .get();
        Object root = storageManager.root();
        if (root == null)
        {
            LOGGER.info("No root yet, creating new instance ");
            root = XReflect.defaultInstantiate(storageBean.getInfo()
                                                       .getClassReference());

            storageManager.setRoot(root);
            storageManager.storeRoot();
        }

        injectDependencies(root, storageBean);

        LOGGER.debug("Executing StorageManagerInitializer beans");

        final List<StorageManagerInitializer> initializers = container
                .select(StorageManagerInitializer.class)
                .stream()
                .collect(Collectors.toList());
        for (final StorageManagerInitializer initializer : initializers)
        {
            initializer.initialize(storageManager);
        }

        return root;

    }

    private void injectDependencies(final Object root, final StorageBean storageBean)
    {
        LOGGER.debug("Injecting instances in @Storage annotated instance");

        final List<String> fieldNamesToInject = storageBean.getInfo()
                .getFieldsToInject();

        final List<Field> fields = ReflectionUtils.findFields(root.getClass(), f -> fieldNamesToInject.contains(f.getName()));
        final ArcContainer container = Arc.container();
        for (final Field field : fields)
        {
            final Object injectable = container.select(field.getType())
                    .get();
            try
            {
                field.setAccessible(true);  // package scope also must be made accessible.
                field.set(root, injectable);
            } catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
