package net.jadoth.persistence.types;



public interface PersistenceTypeDescriptionMemberPseudoFieldSimple
extends PersistenceTypeDescriptionMemberPseudoField
{
	public final class Implementation
	extends PersistenceTypeDescriptionMemberPseudoField.AbstractImplementation
	implements PersistenceTypeDescriptionMemberPseudoFieldSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation New(
			final String  typeName               ,
			final String  name                   ,
			final boolean isReference            ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			return new PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation(
				typeName               ,
				name                   ,
				isReference            ,
				persistentMinimumLength,
				persistentMaximumLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private Implementation(
			final String  typeName           ,
			final String  name               ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(typeName, name, isReference, !isReference, isReference, persistentMinLength, persistentMaxLength);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

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

	}

}
