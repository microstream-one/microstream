
package one.microstream.examples.helloworld;

/*-
 * #%L
 * microstream-examples-helloworld-ini
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

import java.io.IOException;
import java.util.Date;

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;


public class HelloWorld
{
	public static void main(final String[] args) throws IOException
	{
		// Application-specific root instance
		final DataRoot root = new DataRoot();
		
		// configuring the database via .ini file instead of API. Here the directory and the thread count.
		final EmbeddedStorageConfigurationBuilder configuration = EmbeddedStorageConfiguration.load(
			"/META-INF/microstream/storage.ini"
		);
				
		final EmbeddedStorageManager storageManager = configuration
			.createEmbeddedStorageFoundation()
			.createEmbeddedStorageManager(root)
			.start();
				
		// print the root to show its loaded content (stored in the last execution).
		System.out.println(root);

		// Set content data to the root element, including the time to visualize changes on the next execution.
		root.setContent("Hello World! @ " + new Date());

		// Store the modified root and its content.
		storageManager.storeRoot();

		// Shutdown is optional as the storage concept is inherently crash-safe
//		storageManager.shutdown();
		System.exit(0);
	}
}
