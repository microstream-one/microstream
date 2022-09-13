/*-
 * #%L
 * microstream-afs-googlecloud-firestore
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
module microstream.afs.googlecloud.firestore
{
	exports one.microstream.afs.googlecloud.firestore.types;
	
	provides one.microstream.configuration.types.ConfigurationBasedCreator
	    with one.microstream.afs.googlecloud.firestore.types.GoogleCloudFirestoreFileSystemCreator
	;
	
	requires transitive microstream.configuration;
	requires transitive microstream.afs.blobstore;
	requires transitive com.google.api.apicommon;
	requires transitive com.google.auth;
	requires transitive com.google.auth.oauth2;
	requires transitive com.google.protobuf;
	requires transitive gax;
	requires transitive google.cloud.core;
	requires transitive google.cloud.firestore;
}
