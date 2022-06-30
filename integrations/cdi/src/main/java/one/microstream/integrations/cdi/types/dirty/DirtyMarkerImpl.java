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

import one.microstream.integrations.cdi.types.DirtyMarker;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

/**
 * Implementation of the {@link DirtyMarker} interface that passes the instance
 * to the {@link DirtyInstanceCollector} bean for collection.
 */
@RequestScoped
@Typed(DirtyMarker.class)  // We only want the CDI bean to have the interface as Bean Type.
public class DirtyMarkerImpl implements DirtyMarker
{

    @Inject
    private DirtyInstanceCollector collector;

    @Override
    public <T> T mark(final T instance) {
        this.collector.addInstance(instance);
        return instance;
    }
}
