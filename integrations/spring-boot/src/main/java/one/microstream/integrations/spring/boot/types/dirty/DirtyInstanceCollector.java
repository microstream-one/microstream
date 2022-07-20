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

import org.aspectj.lang.reflect.Advice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Collects for each request the instances that are marked as dirty by the developer.
 *
 * @see one.microstream.integrations.spring.boot.types.DirtyMarkerMarker
 */
@Component
@ConditionalOnClass(Advice.class)
public class DirtyInstanceCollector
{

    private final ThreadLocal<List<Object>> dirtyInstances = ThreadLocal.withInitial(ArrayList::new);

    public List<Object> getDirtyInstances()
    {
        return new ArrayList<>(this.dirtyInstances.get());
    }

    public void addInstance(final Object dirty)
    {
        // We can't use HashSet or .equals() like List.contains as that would fail a Unit test that checks
        // If the same object isn't added twice. This needs to be looked into why.

        final Optional<Object> alreadyCollected = this.getDirtyInstances()
                .stream()
                .filter(item -> item == dirty)
                .findAny();

        if (alreadyCollected.isEmpty())
        {
            this.dirtyInstances.get()
                    .add(dirty);
        }
    }

    public void processedInstances()
    {
        this.dirtyInstances.get()
                .clear();
        this.dirtyInstances.remove();
    }
}
