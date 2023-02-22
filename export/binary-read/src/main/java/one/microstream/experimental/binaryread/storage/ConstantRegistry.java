package one.microstream.experimental.binaryread.storage;

/*-
 * #%L
 * binary-read
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceObjectRegistry;

/**
 * A registry of the cached object references that get a different type of reference id.
 */
public final class ConstantRegistry
{

    private static PersistenceObjectRegistry constantRegistry;

    private ConstantRegistry()
    {
    }

    private static void initConstantRegistry()
    {
        constantRegistry = PersistenceObjectRegistry.New();
        Persistence.registerJavaConstants(constantRegistry);
    }

    /**
     * Lookup the object corresponding with the id. This returns null when the id is not known by the Constant registry.
     *
     * @param reference The id of the object to be looked up.
     * @return The Object associated wit the reference or null.
     */
    public static Object lookupObject(final Long reference)
    {
        if (constantRegistry == null)
        {
            initConstantRegistry();
        }
        return constantRegistry.lookupObject(reference);
    }

}
