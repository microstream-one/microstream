package net.jadoth.persistence.binary.types;

import java.lang.reflect.Field;

import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.reflect.XReflect;

public interface BinaryTypeHandler<T> extends PersistenceTypeHandler<Binary, T>
{

	
	public abstract class AbstractImplementation<T>
	extends PersistenceTypeHandler.AbstractImplementation<Binary, T>
	implements BinaryTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final PersistenceTypeDefinitionMemberField declaredField(
			final Class<?> declaringClass,
			final String   fieldName
		)
		{
			final Field field = XReflect.getDeclaredField(declaringClass, fieldName);
			return declaredField(field, BinaryPersistence.createFieldLengthResolver());
		}
		
		public static final PersistenceTypeDefinitionMemberField declaredField(final Field field)
		{
			return declaredField(field, BinaryPersistence.createFieldLengthResolver());
		}
		
			
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final Class<T> type)
		{
			super(type);
		}
		
	}

}
