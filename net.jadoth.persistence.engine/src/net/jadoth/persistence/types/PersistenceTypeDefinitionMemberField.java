package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

import java.lang.reflect.Field;

public interface PersistenceTypeDefinitionMemberField
extends PersistenceTypeDefinitionMember, PersistenceTypeDescriptionMemberField
{
	public Class<?> declaringClass();
	
	public Field field();
		
	public static PersistenceTypeDefinitionMemberField New(
		final Class<?> declaringClass         ,
		final Field    field                  ,
		final Class<?> type                   ,
		final String   typeName               ,
		final String   name                   ,
		final String   declaringTypeName      ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberField.Implementation(
			         declaringClass          ,
			         field                   ,
			         type                    ,
			 notNull(typeName)               ,
			 notNull(name)                   ,
			 notNull(declaringTypeName)      ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public static PersistenceTypeDefinitionMemberField New(
		final Field field                  ,
		final long  persistentMinimumLength,
		final long  persistentMaximumLength
	)
	{
		return New(
			field.getDeclaringClass()          ,
			field                              ,
			field.getType()                    ,
			field.getType().getName()          ,
			field.getName()                    ,
			field.getDeclaringClass().getName(),
			!field.getType().isPrimitive()     ,
			positive(persistentMinimumLength)  ,
			positive(persistentMaximumLength)
		);
	}

	public final class Implementation
	extends PersistenceTypeDescriptionMemberField.Implementation
	implements PersistenceTypeDefinitionMemberField
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private final Class<?> type          ;
		private final Class<?> declaringClass;
		private final Field    field         ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final Class<?> declaringClass     ,
			final Field    field              ,
			final Class<?> type               ,
			final String   typeName           ,
			final String   name               ,
			final String   declaringTypeName  ,
			final boolean  isReference        ,
			final long     persistentMinLength,
			final long     persistentMaxLength
		)
		{
			super(
				typeName           ,
				name               ,
				declaringTypeName  ,
				isReference        ,
				persistentMinLength,
				persistentMaxLength
			);

			this.declaringClass = declaringClass;
			this.field          = field         ;
			this.type           = type          ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> declaringClass()
		{
			return this.declaringClass;
		}
		
		@Override
		public final Field field()
		{
			return this.field;
		}
		
		@Override
		public final Class<?> type()
		{
			return this.type;
		}

	}

}
