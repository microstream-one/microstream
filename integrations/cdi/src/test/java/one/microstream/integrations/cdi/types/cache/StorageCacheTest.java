package one.microstream.integrations.cdi.types.cache;
/*-
 * #%L
 * microstream-integrations-cdi
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

import one.microstream.integrations.cdi.types.test.CDIExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.inject.Inject;

@CDIExtension
public class StorageCacheTest {

    @Inject
    @StorageCache("storage")
    private Cache<Integer, String> cache;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(CacheProperties.STORAGE.get(), Boolean.TRUE.toString());
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(CacheProperties.STORAGE.get());
    }

    @Test
    public void shouldCreateStorableInstance(){
        Assertions.assertNotNull(cache);
    }

}
