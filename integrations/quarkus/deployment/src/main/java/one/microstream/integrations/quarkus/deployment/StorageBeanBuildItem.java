package one.microstream.integrations.quarkus.deployment;

/*-
 * #%L
 * MicroStream Quarkus Extension - Deployment
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



import io.quarkus.builder.item.SimpleBuildItem;
import one.microstream.integrations.quarkus.types.impl.StorageClassInfo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *  {@link io.quarkus.builder.item.BuildItem} that is derived from {@link io.quarkus.arc.deployment.BeanArchiveIndexBuildItem} that keep tracks
 *  of the class annotated with {@link one.microstream.integrations.quarkus.types.Storage}.
 */
public final class StorageBeanBuildItem extends SimpleBuildItem
{

    private final StorageClassInfo rootClassInfo;

    public StorageBeanBuildItem(final StorageClassInfo rootClassInfo)
    {
        this.rootClassInfo = rootClassInfo;
    }

    /**
     * Return the class where {@link one.microstream.storage.types.Storage} is defined or empty optional if
     * not defined by user.
     * @return Class with @Storage or empty.
     */
    public Optional<Class<?>> getRootClass()
    {
        return Optional.ofNullable(this.rootClassInfo).map(StorageClassInfo::getClassReference);
    }


    /**
     * Returns the names of the fields that have {@link javax.inject.Inject} within class annotated with
     * {@link one.microstream.integrations.quarkus.types.Storage}. Empty list when no annotated placed anywhere.
     * @return names of the fields to Inject or empty list if nothing to inject manually.
     */
    public List<String> getFieldsToInject()
    {
        if (this.rootClassInfo == null) {
            return Collections.emptyList();
        } else {
            return this.rootClassInfo.getFieldsToInject();
        }
    }
}
