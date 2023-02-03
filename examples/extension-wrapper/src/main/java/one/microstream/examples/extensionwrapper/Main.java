
package one.microstream.examples.extensionwrapper;

/*-
 * #%L
 * microstream-examples-extension-wrapper
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * Example which shows how to use the instance dispatching in foundations,
 * in order to extend certain parts of the storage engine.
 *
 */
public class Main
{
	private static List<LocalDateTime> ROOT = new ArrayList<>();
	
	
	public static void main(final String[] args)
	{
		// Create default storage foundation
		final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
		
		// Add extender as dispatcher
		foundation.getConnectionFoundation().setInstanceDispatcher(new StorageExtender());
				
		// Start storage
		final EmbeddedStorageManager storage = foundation.start(ROOT);
		
		// See extensions in action
		ROOT.add(LocalDateTime.now());
		storage.storeRoot();
		
		storage.shutdown();
		System.exit(0);
	}
	
}
