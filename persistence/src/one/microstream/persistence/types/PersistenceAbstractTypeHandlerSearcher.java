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
		
		Class<?>   currentClass           = type;
		Class<?>[] currentClassInterfaces = {};

		// keeping track of all superinterfaces of current level interfaces in hierarchical order is ... dizzying.
		final HashEnum<Class<?>> superInterfacesNextLevel = HashEnum.New(); // next or last? My head hurts ...
		
		do
		{
			// always false for the passed class, making the interfaces being covered
			if(XReflect.isAbstract(currentClass))
			{
				abstractSuperTypesInOrder.add(currentClass);
			}

			abstractSuperTypesInOrder.addAll(superInterfacesNextLevel);
			abstractSuperTypesInOrder.addAll(currentClassInterfaces);
			
			superInterfacesNextLevel.clear();
			currentClassInterfaces = currentClass.getInterfaces();
			for(final Class<?> interFace : currentClassInterfaces)
			{
				superInterfacesNextLevel.addAll(interFace.getInterfaces());
			}
			
			currentClass = currentClass.getSuperclass();
		}
		while(currentClass != Object.class);
		
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
