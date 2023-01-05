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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test.microstream.integrations.spring.boot.model.Product;
import test.microstream.integrations.spring.boot.service.NamesService;
import test.microstream.integrations.spring.boot.service.ProductService;

import java.util.Collection;

@SpringBootTest
public class ApplicationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private NamesService namesService;


    @Test
    public void multipleManagerRedTest() {
        Assertions.assertNotNull(productService);

        // Check correct assignment of Root and initialization
        Collection<Product> products = productService.getAll();
        Assertions.assertEquals(3, products.size());

    }

    @Test
    public void multipleManagerGreenTest() {
        Assertions.assertNotNull(namesService);

        // Check correct assignment of Root and initialization
        Collection<String> names = namesService.getAll();
        Assertions.assertEquals(2, names.size());

    }

}
