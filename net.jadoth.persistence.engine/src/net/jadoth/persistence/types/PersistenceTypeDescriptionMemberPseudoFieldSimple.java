package net.jadoth.persistence.types;



public interface PersistenceTypeDescriptionMemberPseudoFieldSimple
extends PersistenceTypeDescriptionMemberPseudoField
{
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		// the type check is the only specific thing here.
		return member instanceof PersistenceTypeDescriptionMemberPseudoFieldSimple
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPseudoFieldSimple)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPseudoFieldSimple m1,
		final PersistenceTypeDescriptionMemberPseudoFieldSimple m2
	)
	{
		// currently no specific checking logic
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	
	
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
		// methods //
		////////////

		@Override
		public void assembleTypeDescription(final Appender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
