package one.microstream.integrations.spring.boot.types.config;

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

import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;


@Configuration
public class StorageManagerFactory
{

    private final StorageManagerProvider storageManagerProvider;

    public StorageManagerFactory(
            StorageManagerProvider storageManagerProvider
    )
    {
        this.storageManagerProvider = storageManagerProvider;
    }

    @Bean
    @Lazy
    @Primary
    public StorageManagerConfiguration storageManagerConfiguration ()
    {
        return this.storageManagerProvider.prepareConfiguration();
    }

    @Bean
    @Lazy
    @Primary
    public EmbeddedStorageFoundation<?> embeddedStorageFoundation()
    {
        return storageManagerProvider.embeddedStorageFoundation();
    }

    @Bean(destroyMethod = "shutdown")
    @Lazy
    @Primary
    public EmbeddedStorageManager embeddedStorageManager()
    {
        return storageManagerProvider.get(StorageManagerProvider.PRIMARY_QUALIFIER);
    }

}
