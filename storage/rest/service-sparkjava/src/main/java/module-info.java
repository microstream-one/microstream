/*-
 * #%L
 * microstream-storage-restservice-sparkjava
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
module microstream.storage.restservice.sparkjava
{
	exports one.microstream.storage.restservice.sparkjava.exceptions;
	exports one.microstream.storage.restservice.sparkjava.types;
	
	provides one.microstream.storage.restadapter.types.StorageViewDataConverter
	    with one.microstream.storage.restservice.sparkjava.types.StorageViewDataConverterJson
	;
	provides one.microstream.storage.restservice.types.StorageRestServiceProvider
	    with one.microstream.storage.restservice.sparkjava.types.StorageRestServiceProviderSparkJava
	;
	
	requires transitive microstream.storage.restservice;
	requires transitive com.google.gson;
	requires transitive spark.core;
}
