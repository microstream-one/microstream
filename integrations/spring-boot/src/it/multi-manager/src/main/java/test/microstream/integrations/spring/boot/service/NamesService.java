package test.microstream.integrations.spring.boot.service;

/*-
 * #%L
 * microstream-integrations-spring-boot
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

import org.springframework.stereotype.Service;
import test.microstream.integrations.spring.boot.database.Names;

import java.util.Collection;

@Service
public class NamesService {


    private final Names greenRoot;

    public NamesService(final Names greenRoot) {
        this.greenRoot = greenRoot;
    }

    public Collection<String> getAll() {
        return this.greenRoot.getNames();
    }


}
