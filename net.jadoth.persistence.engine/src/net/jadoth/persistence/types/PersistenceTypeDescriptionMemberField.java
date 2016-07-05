package net.jadoth.persistence.types;



public interface PersistenceTypeDescriptionMemberField extends PersistenceTypeDescriptionMember
{
	public String declaringTypeName();

	public String qualifiedFieldName();

	// (14.08.2015 TM)TODO: include Generics, Field#getGenericType
//	public String typeParamterString();



	public static PersistenceTypeDescriptionMemberField New(
		final String  typeName               ,
		final String  name                   ,
		final String  declaringTypeName      ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberField.Implementation(
			typeName               ,
			name                   ,
			declaringTypeName      ,
			isReference            ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}

	public final class Implementation
	extends PersistenceTypeDescriptionMember.AbstractImplementation
	implements PersistenceTypeDescriptionMemberField
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final           String declaringTypeName ;
		private final transient String qualifiedFieldName;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final String  typeName           ,
			final String  name               ,
			final String  declaringTypeName  ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(typeName, name, isReference, !isReference, false, isReference, persistentMinLength, persistentMaxLength);
			this.declaringTypeName = declaringTypeName;
			this.qualifiedFieldName = PersistenceTypeDictionary.fullQualifiedFieldName(declaringTypeName, name);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public String declaringTypeName()
		{
			return this.declaringTypeName;
		}

		@Override
		public String qualifiedFieldName()
		{
			return this.qualifiedFieldName;
		}

		@Override
		public void assembleTypeDescription(final Appender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

		@Override
		public boolean equals(final PersistenceTypeDescriptionMember m2, final DescriptionMemberEqualator equalator)
		{
			return equalator.equals(this, m2);
		}

		@Override
		public String toString()
		{
			return this.typeName() + ' ' + this.qualifiedFieldName();
		}

	}

}
