package net.jadoth.persistence.types;

import java.lang.reflect.Field;

public interface PersistenceTypeDefinitionMemberField<O>
extends PersistenceTypeDefinitionMember<O>, PersistenceTypeDescriptionMemberField
{
	public Class<?> declaringClass();
	
	public Field field();
		
	public static <O>  PersistenceTypeDefinitionMemberField<O> New(
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
		return new PersistenceTypeDefinitionMemberField.Implementation<>(
			declaringClass         ,
			field                  ,
			type                   ,
			typeName               ,
			name                   ,
			declaringTypeName      ,
			isReference            ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}
	
	public static <O> PersistenceTypeDefinitionMemberField<O> New(
		final PersistenceTypeDefinition<O> ownerType              ,
		final Field                        field                  ,
		final long                         persistentMinimumLength,
		final long                         persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberField.Implementation<>(
			field.getDeclaringClass()          ,
			field                              ,
			field.getType()                    ,
			field.getType().getName()          ,
			field.getName()                    ,
			field.getDeclaringClass().getName(),
			!field.getType().isPrimitive()     ,
			persistentMinimumLength            ,
			persistentMaximumLength
		);
	}

	public final class Implementation<O> extends PersistenceTypeDescriptionMember.AbstractImplementation
	implements
	PersistenceTypeDefinitionMemberField<O>,
	PersistenceTypeDefinitionMember.EffectiveFinalOwnerTypeHolder<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private final Class<?>                     type             ;
		private /*f*/ PersistenceTypeDefinition<O> ownerType        ;
		private final Class<?>                     declaringClass   ;
		private final Field                        field            ;
		private final String                       declaringTypeName;
		
		private final transient String qualifiedFieldName;



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
				isReference        ,
				!isReference       ,
				false              ,
				isReference        ,
				persistentMinLength,
				persistentMaxLength
			);

			this.declaringClass     = declaringClass   ;
			this.field              = field            ;
			this.type               = type             ;
			this.declaringTypeName  = declaringTypeName;
			this.qualifiedFieldName = PersistenceTypeDictionary.fullQualifiedFieldName(declaringTypeName, name);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceTypeDefinition<O> ownerType()
		{
			return this.ownerType;
		}
		
		@Override
		public final void internalSetValidatedOwnerType(final PersistenceTypeDefinition<O> ownerType)
		{
			this.ownerType = ownerType;
		}
		
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

		@Override
		public final String declaringTypeName()
		{
			return this.declaringTypeName;
		}

		@Override
		public final String uniqueName()
		{
			return this.qualifiedFieldName;
		}

		@Override
		public void assembleTypeDescription(final Appender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
