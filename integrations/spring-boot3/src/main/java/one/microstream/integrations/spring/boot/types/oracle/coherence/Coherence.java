package one.microstream.integrations.spring.boot.types.oracle.coherence;

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

public class Coherence
{

    /**
     * The name of the cache which is used to retrieve the named cache from the cache factory.
     */
    private String cacheName;

    /**
     * It is used to specify a custom cache configuration deployment descriptor to be used instead of the configured default cache configuration deployment descriptor.
     */
    private String cacheConfig;

    public String getCacheName()
    {
        return cacheName;
    }

    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }

    public String getCacheConfig()
    {
        return cacheConfig;
    }

    public void setCacheConfig(String cacheConfig)
    {
        this.cacheConfig = cacheConfig;
    }
}
