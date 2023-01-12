package one.microstream.integrations.spring.boot.types.util;

/*-
 * #%L
 * microstream-integrations-spring-boot-common
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

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Collections;
import java.util.Map;

public class EnvironmentFromMap extends AbstractEnvironment
{

    public EnvironmentFromMap(final Map<String, String> values)
    {
        // customizePropertySources(MutablePropertySources propertySources)
        // is useless in our case as called by super constructor and thus no access to our Map with Strings
        this.getPropertySources()
                .addLast(
                        new MapPropertySource("FromMap", Collections.unmodifiableMap(values))
                );
    }
}
