package one.microstream.integrations.spring.boot.types.storage;

/*-
 * #%L
 * microstream-integrations-spring-boot3
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

import one.microstream.integrations.spring.boot.types.config.StorageManagerProvider;
import org.springframework.beans.factory.annotation.Qualifier;

public class StorageClassData
{

    private final Class<?> clazz;  // The Root class

    private final String qualifier;  // The qualifier

    public StorageClassData(final Class<?> clazz)
    {
        this.clazz = clazz;
        this.qualifier = findQualifier(clazz);
    }

    private String findQualifier(final Class<?> clazz)
    {
        Qualifier qualifier = clazz.getAnnotation(Qualifier.class);
        if (qualifier != null)
        {
            return qualifier.value();
        }
        // No qualifier or @Primary
        return StorageManagerProvider.PRIMARY_QUALIFIER;
    }

    public Class getClazz()
    {
        return this.clazz;
    }

    public String getQualifier()
    {
        return this.qualifier;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof StorageClassData))
        {
            return false;
        }

        StorageClassData that = (StorageClassData) o;

        if (!this.clazz.equals(that.clazz))
        {
            return false;
        }
        return this.qualifier.equals(that.qualifier);
    }

    @Override
    public int hashCode()
    {
        int result = this.clazz.hashCode();
        result = 31 * result + this.qualifier.hashCode();
        return result;
    }
}
