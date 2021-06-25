/*-
 * #%L
 * microstream-cache
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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
	exports one.microstream.cache.exceptions;
	
	requires cache.api;
	requires java.management;
	requires microstream.afs;
	requires microstream.base;
	requires microstream.configuration;
	requires microstream.persistence;
	requires microstream.persistence.binary;
	requires microstream.storage;
	requires microstream.storage.embedded;
	requires microstream.storage.embedded.configuration;
}
