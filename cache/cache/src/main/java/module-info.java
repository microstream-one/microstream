/*-
 * #%L
 * microstream-cache
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
module microstream.cache
{
	exports one.microstream.cache.types;
	
	provides javax.cache.spi.CachingProvider
	    with one.microstream.cache.types.CachingProvider
	;
	
	requires transitive microstream.persistence.binary;
	requires transitive microstream.storage.embedded.configuration;
	requires transitive cache.api;
	requires transitive java.management;
}
