
package one.microstream.examples.cdi.javase;

/*-
 * #%L
 * microstream-examples-cdi-javase
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

import one.microstream.storage.types.StorageManager;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

/**
 * Testing the StorageManager integration.
 */
public class App
{
	public static void main(final String[] args)
	{
		try(SeContainer container = SeContainerInitializer.newInstance().initialize())
		{

			final StorageManager manager = container.select(StorageManager.class)
					.get();
			final Object root = manager.root();
			System.out.println("The root value: " + root);
			final NamesService service = container.select(NamesService.class)
					.get();

			System.out.println("The names: " + service.getNames());
			service.add("Sebastian");
			service.add("Otavio");
			service.add("Ada");
			service.add("Mari");
		}
		System.exit(0);
	}
}
