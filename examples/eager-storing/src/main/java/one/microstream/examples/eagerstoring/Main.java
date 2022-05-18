package one.microstream.examples.eagerstoring;

/*-
 * #%L
 * microstream-examples-eager-storing
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

import java.time.LocalDateTime;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * Excample which shows how to implement a custom eager storing behavior,
 * based on an annotation ({@link StoreEager})
 * and a field evaluator ({@link StoreEagerEvaluator}).
 * 
 *
 */
public class Main
{
	public static void main(
		final String[] args
	)
	{
		for(int i = 0; i < 5; i++)
		{
			final EmbeddedStorageManager storage = EmbeddedStorage.Foundation()
				// register custom field evaluator
				.onConnectionFoundation(cf -> cf.setReferenceFieldEagerEvaluator(new StoreEagerEvaluator()))
				.createEmbeddedStorageManager()
				.start()
			;
			if(storage.root() == null)
			{
				final MyRoot root = new MyRoot();
				root.numbers.add(1);
				root.dates.add(LocalDateTime.now());
				storage.setRoot(root);
				storage.storeRoot();
			}
			else
			{
				final MyRoot root = (MyRoot)storage.root();
				
				/*
				 * At each iteration you can see that the eager field (root#numbers) gets stored,
				 * whereas root#dates is not, because it is not marked as eager.
				 */
				System.out.println(root.numbers + ", " + root.dates);
				
				root.numbers.add(root.numbers.size() + 1);
				root.dates.add(LocalDateTime.now());
				storage.storeRoot();
			}
			
			storage.shutdown();
		}
	}
	
}
