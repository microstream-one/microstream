/*
 * Copyright 2021-2022 Rudy De Busscher (https://www.atbash.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.microstream.integrations.cdi.types.dirty;

/*-
 * #%L
 * MicroStream Integrations CDI
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

import javax.enterprise.context.RequestScoped;
import java.util.*;

/**
 * Collects for each request the instances that are marked as dirty by the developer.
 *
 * @see one.microstream.integrations.cdi.types.DirtyMarker
 */
@RequestScoped
public class DirtyInstanceCollector
{

    private final List<Object> dirtyInstances = new ArrayList<>();

    public List<Object> getDirtyInstances()
    {
        return new ArrayList<>(this.dirtyInstances);
    }

    public void addInstance(final Object dirty)
    {
        // We can't use HashSet or .equals() like List.contains as that would fail a Unit test that checks
        // If the same object isn't added twice. This needs to be looked into why.
        final Optional<Object> alreadyCollected = this.dirtyInstances.stream()
                .filter(item -> item == dirty)
                .findAny();
        if (alreadyCollected.isEmpty())
        {
            this.dirtyInstances.add(dirty);
        }
    }

    public void processedInstances()
    {
        this.dirtyInstances.clear();
    }
}
