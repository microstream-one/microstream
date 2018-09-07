package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;

public interface PersistenceTypeDescriptionMemberPseudoFieldComplex
extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength
{
	public XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members();

	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberPseudoFieldComplex
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPseudoFieldComplex)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPseudoFieldComplex m1,
		final PersistenceTypeDescriptionMemberPseudoFieldComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2)
			&& PersistenceTypeDescriptionMember.equalDescriptions(m1.members(), m2.members())
		;
	}


	public final class Implementation
	extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Implementation
	implements PersistenceTypeDescriptionMemberPseudoFieldComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final XImmutableSequence<PersistenceTypeDescriptionMemberPseudoField> members;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final String                                                        name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members                ,
			final long                                                          persistentMinimumLength,
			final long                                                          persistentMaximumLength
		)
		{
			super(
				PersistenceTypeDictionary.Symbols.TYPE_COMPLEX,
				name,
				PersistenceTypeDescriptionMember.determineHasReferences(members),
				persistentMinimumLength,
				persistentMaximumLength
			);
			this.members = members.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members()
		{
			return this.members;
		}

		@Override
		public void assembleTypeDescription(final Appender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
