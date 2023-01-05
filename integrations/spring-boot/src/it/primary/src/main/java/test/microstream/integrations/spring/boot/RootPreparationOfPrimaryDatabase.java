package test.microstream.integrations.spring.boot;

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

import one.microstream.integrations.spring.boot.types.config.StorageManagerInitializer;
import one.microstream.integrations.spring.boot.types.config.StorageManagerProvider;
import one.microstream.storage.types.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import test.microstream.integrations.spring.boot.database.Products;
import test.microstream.integrations.spring.boot.model.Product;

@Component
public class RootPreparationOfPrimaryDatabase implements StorageManagerInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootPreparationOfPrimaryDatabase.class);

    @Override
    public void initialize(final StorageManager storageManager) {
        if (!StorageManagerProvider.PRIMARY_QUALIFIER.equals(storageManager.databaseName())) {
            // This customizer operates on the Primary database
            return;
        }

        LOGGER.info("(From the App) Add basic data if needed (For Root of Red database)");

        // Since we have @Storage used, we are sure that Root object is initialized in StorageManager
        // We only need to check if there is an initialization of data required or not (since we already ran it before)

        Products root = (Products) storageManager.root();
        // Init 'database' with some data
        if (root.getProducts().isEmpty()) {
            init(root);
        }

    }

    public void init(final Products root) {
        root.addProduct(new Product(1L, "Apple", 5));
        root.addProduct(new Product(2L, "Banana", 4));
        root.addProduct(new Product(3L, "Kiwi", 2));
    }


}
