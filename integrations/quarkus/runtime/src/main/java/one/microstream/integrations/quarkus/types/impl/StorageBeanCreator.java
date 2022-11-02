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

import io.quarkus.arc.BeanCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import java.util.Map;

public class StorageBeanCreator implements BeanCreator<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBeanCreator.class);

    @Override
    public Object create(final CreationalContext creationalContext, final Map map)
    {
        LOGGER.debug("Create Bean: Creating bean with info about @Storage annotated class");

        final StorageClassInfo storageClassInfo = new StorageClassInfo((Class<?>) map.get(BeanCreatorParameterNames.CLASS_NAME)
                , (String) map.get(BeanCreatorParameterNames.FIELDS));

        return new StorageBean(storageClassInfo);

    }

}
