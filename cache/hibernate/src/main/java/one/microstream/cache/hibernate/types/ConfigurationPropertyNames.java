package one.microstream.cache.hibernate.types;

/*-
 * #%L
 * microstream-cache-hibernate
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

public interface ConfigurationPropertyNames
{
	public static final String PREFIX                      = "hibernate.cache.microstream.";
	                                                 
	public static final String CACHE_MANAGER               = PREFIX + "cache-manager";
	public static final String MISSING_CACHE_STRATEGY      = PREFIX + "missing-cache-strategy";
	public static final String CACHE_LOCK_TIMEOUT          = PREFIX + "cache-lock-timeout";
	public static final String CONFIGURATION_RESOURCE_NAME = PREFIX + "configuration-resource-name";
}
