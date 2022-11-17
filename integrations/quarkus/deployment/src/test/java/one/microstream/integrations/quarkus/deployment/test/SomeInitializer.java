package one.microstream.integrations.quarkus.deployment.test;

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

import one.microstream.integrations.quarkus.types.config.StorageManagerInitializer;
import one.microstream.storage.types.StorageManager;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SomeInitializer implements StorageManagerInitializer
{
    @Override
    public void initialize(StorageManager storageManager)
    {
        Object rootObject = storageManager.root();
        if (rootObject == null) {
            SomeRoot root = new SomeRoot();
            root.setData("Initial value");
            storageManager.setRoot(root);
        } else {
            throw new IllegalStateException("StorageManager should not have already a Root object assigned");
        }
    }
}
