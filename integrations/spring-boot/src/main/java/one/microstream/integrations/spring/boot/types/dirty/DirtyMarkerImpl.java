package one.microstream.integrations.spring.boot.types.dirty;

/*-
 * #%L
 * microstream-integrations-spring-boot
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


import one.microstream.integrations.spring.boot.types.DirtyMarker;
import org.aspectj.lang.reflect.Advice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link DirtyMarker} interface that passes the instance
 * to the {@link DirtyInstanceCollector} bean for collection.
 */

@Component
@ConditionalOnClass(Advice.class)
public class DirtyMarkerImpl implements DirtyMarker
{

    private final DirtyInstanceCollector collector;

    public DirtyMarkerImpl(DirtyInstanceCollector collector)
    {
        this.collector = collector;
    }

    @Override
    public <T> T mark(final T instance)
    {
        this.collector.addInstance(instance);
        return instance;
    }
}
