package one.microstream.examples.layeredentities;

/*-
 * #%L
 * microstream-examples-layered-entities
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

import one.microstream.collections.types.XGettingTable;
import one.microstream.entity.Entity;
import one.microstream.entity.EntityVersionContext;
import one.microstream.examples.layeredentities._Human.HumanUpdater;

public class LayeredEntities
{
	public static void main(final String[] args)
	{
		final Human human = EntityFactory.HumanCreator()
			.name("John Doe")
			.address(
				EntityFactory.AddressCreator()
					.street("Main Street")
					.city("Springfield")
					.create()
			)
			.create();
		
		HumanUpdater.setAddress(
			human,
			EntityFactory.AddressCreator()
				.street("Rose Boulevard")
				.city("Newtown")
				.create()
		);
		
		printVersions(human);
	}

	static void printVersions(final Entity entity)
	{
		final EntityVersionContext<Integer>  context  = EntityVersionContext.lookup(entity);
		final XGettingTable<Integer, Entity> versions = context.versions(entity);
		versions.iterate(v ->
			System.out.println("Version " + v.key() + " = " + v.value())
		);
	}
	
}
