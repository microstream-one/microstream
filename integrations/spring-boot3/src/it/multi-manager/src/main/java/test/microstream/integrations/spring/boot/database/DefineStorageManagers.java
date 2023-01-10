package test.microstream.integrations.spring.boot.database;

/*-
 * #%L
 * microstream-integrations-spring-boot3
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import one.microstream.integrations.spring.boot.types.config.StorageManagerProvider;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefineStorageManagers {

    private final StorageManagerProvider provider;

    public DefineStorageManagers(StorageManagerProvider provider) {
        this.provider = provider;
    }

    @Bean
    @Qualifier("green")
    public EmbeddedStorageManager getGreenManager() {
        return this.provider.get(DatabaseColor.GREEN.getName());
    }

    @Bean
    @Qualifier("red")
    public EmbeddedStorageManager getRedManager() {
        return this.provider.get(DatabaseColor.RED.getName());
    }
}
