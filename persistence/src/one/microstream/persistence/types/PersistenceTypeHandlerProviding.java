package one.microstream.persistence.types;

import java.lang.reflect.Method;

import one.microstream.reflect.XReflect;

/**
 * This is a mere marker interface to indicate that a class implementing it also contains a static method with
 * arbitrary name and visibility, no arguments and {@link PersistenceTypeHandler} or a sub type of it as its
 * return type.<p>
 * Entities of a class implementing this interface will be handled by the {@link PersistenceTypeHandler}
 * instance returned by the described method. Which method to select is also determined by tests of the
 * returned {@link PersistenceTypeHandler} instance has the correct {@link PersistenceTypeHandler#dataType()}
 * for the used persistence context and the correct {@link PersistenceTypeHandler#type()} for the given entity class.
 * <p>
 * This mechanism is a convenience shortcut alternative to
 * {@link PersistenceFoundation#registerCustomTypeHandler(PersistenceTypeHandler)}.
 * 
 * @author TM
 */
public interface PersistenceTypeHandlerProviding
{
	/*
	 * example on how the expected method in the entity class (here for a class called "Person") could look like.
	 * Note that both visibility and name of the method are irrelevant.
	 */
//	static PersistenceTypeHandler<Binary, Person> provideTypeHandler()
//	{
//		return new MyCustomPersonBinaryTypeHandler();
//	}
	
	
	
	public static <D, T> PersistenceTypeHandler<D, T> searchProvidedTypeHandler(
		final Class<D> dataType  ,
		final Class<T> entityType
	)
		throws ReflectiveOperationException
	{
		// ONLY declared methods of the specific type, not of super classes, since every class needs a specific handler
		for(final Method m : entityType.getDeclaredMethods())
		{
			// only static methods are admissible.
			if(!XReflect.isStatic(m))
			{
				continue;
			}

			// only parameter-less methods are admissible.
			if(m.getParameterCount() != 0)
			{
				continue;
			}

			// only methods returning an instance of PersistenceTypeHandler are admissible.
			if(!PersistenceTypeHandler.class.isAssignableFrom(m.getReturnType()))
			{
				continue;
			}
			
			m.setAccessible(true);
			final PersistenceTypeHandler<?, ?> providedTypeHandler = (PersistenceTypeHandler<?, ?>)m.invoke(null);
			
			// context checks
			if(providedTypeHandler.dataType() != dataType)
			{
				continue;
			}
			if(providedTypeHandler.type() != entityType)
			{
				continue;
			}

			@SuppressWarnings("unchecked")
			final PersistenceTypeHandler<D, T> applicableTypeHandler = (PersistenceTypeHandler<D, T>)providedTypeHandler;
			
			return applicableTypeHandler;
		}
		
		return null;
	}
	
}
