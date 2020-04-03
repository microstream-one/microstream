package one.microstream.persistence.types;

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
		final HashEnum<Class<?>> abstractSuperTypesInOrder = HashEnum.New();
		final long collectCount = abstractSuperTypesInOrder.size();
		
		Class<?>   currentClass           = type;
		Class<?>[] currentClassInterfaces = {};

		// keeping track of all superinterfaces of current level interfaces in hierarchical order is ... dizzying.
		final HashEnum<Class<?>> superInterfacesNextLevel = HashEnum.New(); // next or last? My head hurts ...
		
		/*
		 * There are 3 conditions that keep the interface collecting loop going:
		 * - There are still direct super classes in the hierarchy to be checked
		 * - There are still "next level" interfaces to be checked collected from the last cycle's interfaces
		 * -
		 */
		while(currentClass != Object.class || abstractSuperTypesInOrder.size() > collectCount)
		{
			// add current (super) class with higher priority than interfaces, but only if abstract
			Default.addAbstractClass(superInterfacesNextLevel, currentClass);

			// add last class's interfaces with second highest priority for this cycle
			abstractSuperTypesInOrder.addAll(currentClassInterfaces);

			// add last class's interface-interfaces with least highest priority for this cycle
			abstractSuperTypesInOrder.addAll(superInterfacesNextLevel);
			
			currentClassInterfaces = currentClass.getInterfaces();
			
			superInterfacesNextLevel.clear();
			Default.collectAllInterfaces(superInterfacesNextLevel, currentClassInterfaces);

			// stick at Object.class to avoid null-checks
			currentClass = XReflect.getSuperClassNonNull(currentClass);
		}
		
		
		// (03.04.2020 TM)FIXME: priv#187: this is not correct, yet

		// don't forget the interfaces
		abstractSuperTypesInOrder.addAll(superInterfacesNextLevel);
		abstractSuperTypesInOrder.addAll(currentClassInterfaces);

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
		
		static final void collectAllInterfaces(
			final HashEnum<Class<?>> collection,
			final Class<?>[]         classes
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
