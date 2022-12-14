package one.microstream.integrations.spring.boot.types.storage;

/*-
 * #%L
 * microstream-integrations-spring-boot3
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

/**
 * We need to keep track if there is a @Storage annotated class to know when the StorageManagerInitializer needs to be called.
 * This class will be turned into a bean and placed into the Context if there is such a bean.
 */
public class StorageMetaData
{
    private final Class<?> storageClassName;

    public StorageMetaData(final Class<?> storageClassName)
    {
        this.storageClassName = storageClassName;
    }

    public Class<?> getStorageClassName()
    {
        return this.storageClassName;
    }
}
