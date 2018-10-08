package net.jadoth.persistence.binary.types;

import java.lang.reflect.Field;

import net.jadoth.X;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberField;
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
		
		public static final PersistenceTypeDescriptionMemberField declaredField(
			final Class<?> declaringClass,
			final String   fieldName
		)
		{
			final Field field = XReflect.getDeclaredField(declaringClass, fieldName);
			return declaredField(field, new BinaryFieldLengthResolver.Implementation());
		}
		
		public static final PersistenceTypeDescriptionMemberField declaredField(final Field field)
		{
			return declaredField(field, new BinaryFieldLengthResolver.Implementation());
		}
		
		public static final PersistenceTypeDescriptionMemberField declaredField(
			final Field                          field         ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			return PersistenceTypeDescriptionMemberField.New(
				field                                              ,
				lengthResolver.resolveMinimumLengthFromField(field),
				lengthResolver.resolveMaximumLengthFromField(field)
			);
		}
		
		public static final XImmutableSequence<PersistenceTypeDescriptionMemberField> declaredFields(
			final PersistenceTypeDescriptionMemberField... declaredFields
		)
		{
			return X.ConstList(declaredFields);
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
