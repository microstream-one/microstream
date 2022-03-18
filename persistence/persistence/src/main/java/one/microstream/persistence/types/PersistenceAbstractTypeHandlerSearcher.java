package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import one.microstream.collections.BulkList;
import one.microstream.collections.HashEnum;
import one.microstream.reflect.XReflect;

public interface PersistenceAbstractTypeHandlerSearcher<D>
{
	public default <T> PersistenceTypeHandler<D, ? super T> searchAbstractTypeHandler(
		final Class<T>                                type                     ,
		final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry
	)
	{
		return PersistenceAbstractTypeHandlerSearcher.searchAbstractTypeHandler(
			customTypeHandlerRegistry,
			type
		);
	}
	
	
	
	public static <D, T> PersistenceTypeHandler<D, ? super T> searchAbstractTypeHandler(
		final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry,
		final Class<T>                                type
	)
	{
		final HashEnum<Class<?>> abstractSuperTypesInOrder       = HashEnum.New();
		final HashEnum<Class<?>> abstractTypesForNextLevel       = HashEnum.New();
		final BulkList<Class<?>> abstractTypesToAddFromLastLevel = BulkList.New();
		
		Class<?> currentClass = type;
		
		/*
		 * There are 2 conditions that keep the interface collecting loop going:
		 * - There are still super classes in the class hierarchy to be checked
		 * - There are still interface hierarchy interfaces (interface hierarchy is "deeper" than class hierarchy)
		 * 
		 * Note that just the count check would not be enough since a class can have a non-abstract superclass
		 * and no interfaces. So the count would be 0 for the lowest level and the loop would abort prematurely.
		 */
		while(currentClass != Object.class || !abstractTypesToAddFromLastLevel.isEmpty())
		{
			// "currentClass" is actually the previous class at the start of the cycle
			final Class<?>[] previousClassInterfaces = currentClass.getInterfaces();

			// get actual "current" class for this cycle from previous class.
			currentClass = XReflect.getSuperClassNonNull(currentClass);
			
			// add current class with higher priority than previous level's interfaces, but only if abstract
			Default.addAbstractClass(abstractTypesForNextLevel, currentClass);
			
			// add previous class's interfaces with secondary priority
			abstractTypesForNextLevel.addAll(previousClassInterfaces);
			Default.collectAllSuperInterfaces(abstractTypesForNextLevel, abstractTypesToAddFromLastLevel);
			
			// add last hierarchy level's interfaces with second highest priority for this cycle
			abstractSuperTypesInOrder.addAll(abstractTypesToAddFromLastLevel);
			abstractTypesToAddFromLastLevel.clear();
			
			// move all "next level" types to "toAdd" collection for next cycle.
			abstractTypesToAddFromLastLevel.addAll(abstractTypesForNextLevel);
			abstractTypesForNextLevel.clear();
		}

		PersistenceTypeHandler<D, ?> abstractTypeHandler = null;
		for(final Class<?> abstractSuperType : abstractSuperTypesInOrder)
		{
			abstractTypeHandler = customTypeHandlerRegistry.lookupTypeHandler(abstractSuperType);
			if(abstractTypeHandler != null)
			{
				break;
			}
		}
		
		@SuppressWarnings("unchecked")
		final PersistenceTypeHandler<D, ? super T> result =
			(PersistenceTypeHandler<D, ? super T>)abstractTypeHandler
		;
		
		return result;
	}
	

	
	
	public static <D> PersistenceAbstractTypeHandlerSearcher<D> New()
	{
		return new PersistenceAbstractTypeHandlerSearcher.Default<>();
	}
	
	public final class Default<D> implements PersistenceAbstractTypeHandlerSearcher<D>
	{
		// actually belongs in the interface, but JLS' visibility rules are too stupid to allow clean architecture.
		static final void addAbstractClass(
			final HashEnum<Class<?>> collection,
			final Class<?>           clazz
		)
		{
			if(!XReflect.isAbstract(clazz))
			{
				return;
			}
			
			collection.add(clazz);
		}
		
		static final void collectAllSuperInterfaces(
			final HashEnum<Class<?>> collection,
			final Iterable<Class<?>> classes
		)
		{
			for(final Class<?> c : classes)
			{
				collection.addAll(c.getInterfaces());
			}
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}

		// that's all, folks!
	}
	
}
