package one.microstream.integrations.spring.boot.types.hazelcast;

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

public class Hazelcast
{

    /**
     * Supported values:
     * <ul>
     * <li>"default"</li>
     * It tries to load Hazelcast configuration from a list of well-known locations, and then applies overrides found in environment variables/system properties. When no location contains Hazelcast configuration then it returns default.
     *
     * <li>"classpath:path-to-hazelcast-configuration-file"</li>
     * When the "classpath:" prefix is used, the file is loaded from a classpath resource.
     *
     * <li>a valid URL</li>
     * Creates new Config which is loaded from the given URL and uses the System.properties to replace variables.
     *
     * <li>a file path</li>
     * Creates a Config based on a Hazelcast file and uses the System.properties to resolve variables.
     * </ul>
     */
    String configuration;
}
