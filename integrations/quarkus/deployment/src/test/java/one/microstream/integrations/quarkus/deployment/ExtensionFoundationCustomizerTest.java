package one.microstream.integrations.quarkus.deployment;

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

import io.quarkus.test.QuarkusUnitTest;
import one.microstream.integrations.quarkus.deployment.test.SomeCustomizer;
import one.microstream.storage.types.StorageManager;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import java.io.File;


public class ExtensionFoundationCustomizerTest
{

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() ->
                                        ShrinkWrap.create(JavaArchive.class)
                                                .addClasses(SomeCustomizer.class, CleanupUtil.class)
                                                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );

    @AfterEach
    public void cleanup()
    {
        CleanupUtil.deleteDirectory(new File("storage"));
    }

    @Inject
    StorageManager storageManager;

    @Test
    public void testCustomizer()
    {
        Assertions.assertNotNull(storageManager);
        Assertions.assertNull(storageManager.root());
        Assertions.assertTrue(storageManager.isRunning());
        Assertions.assertEquals("JUnit", storageManager.databaseName());
    }
}
