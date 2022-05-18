package one.microstream.examples.layeredentities;

/*-
 * #%L
 * microstream-examples-layered-entities
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

import one.microstream.entity.Entity;
import one.microstream.entity.EntityVersionCleaner;
import one.microstream.entity.EntityVersionContext;
import one.microstream.examples.layeredentities._Address.AddressCreator;
import one.microstream.examples.layeredentities._Animal.AnimalCreator;
import one.microstream.examples.layeredentities._Human.HumanCreator;
import one.microstream.examples.layeredentities._Pet.PetCreator;


public final class EntityFactory
{
	final static JulLogger                     logger  = new JulLogger();
	final static EntityVersionCleaner<Integer> cleaner = EntityVersionCleaner.AmountPreserving(10);
	
	public static AddressCreator AddressCreator()
	{
		return addLayers(AddressCreator.New());
	}
	
	public static AnimalCreator AnimalCreator()
	{
		return addLayers(AnimalCreator.New());
	}
	
	public static HumanCreator HumanCreator()
	{
		return addLayers(HumanCreator.New());
	}
	
	public static PetCreator PetCreator()
	{
		return addLayers(PetCreator.New());
	}
	
	private static <E extends Entity, C extends Entity.Creator<E, C>> C addLayers(final C creator)
	{
		return creator
			.addLayer(logger)
			.addLayer(EntityVersionContext.AutoIncrementingInt(cleaner))
		;
	}
	
	private EntityFactory()
	{
	}
}
