package one.microstream.persistence.binary.types;

import java.lang.reflect.Field;

import one.microstream.persistence.types.PersistenceTypeDefinitionMemberField;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.reflect.XReflect;

public interface BinaryTypeHandler<T> extends PersistenceTypeHandler<Binary, T>
{
	public abstract class Abstract<T>
	extends PersistenceTypeHandler.Abstract<Binary, T>
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

		protected Abstract(final Class<T> type)
		{
			super(type);
		}
		
	}

}
