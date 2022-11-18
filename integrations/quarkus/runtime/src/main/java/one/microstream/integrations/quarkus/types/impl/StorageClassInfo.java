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

import java.util.Arrays;
import java.util.List;

/**
 * Information about a class annotated with {@link one.microstream.integrations.quarkus.types.Storage}.
 */
public class StorageClassInfo
{

    private final Class<?> classReference;

    private final List<String> fieldsToInject;

    public StorageClassInfo(final Class<?> classReference, final List<String> fieldsToInject)
    {

        this.classReference = classReference;
        this.fieldsToInject = fieldsToInject;
    }

    public StorageClassInfo(final Class<?> classReference, final String fieldsToInject)
    {
        if (classReference != Object.class)
        {
            this.classReference = classReference;
        } else {
            // Object.class past since .map entries on BeanCreator blow up when null is specified in value.
            // see https://github.com/quarkusio/quarkus/issues/27664
            this.classReference = null;
        }
        this.fieldsToInject = Arrays.asList(fieldsToInject.split(","));
    }

    public Class<?> getClassReference()
    {
        return classReference;
    }

    public List<String> getFieldsToInject()
    {
        return this.fieldsToInject;
    }
}
