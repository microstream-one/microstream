package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;

public interface PersistenceTypeDescriptionMemberPseudoFieldComplex
extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength
{
	public XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members();



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
		// override methods //
		/////////////////////

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

		@Override
		public boolean equals(final PersistenceTypeDescriptionMember m2, final DescriptionMemberEqualator equalator)
		{
			return equalator.equals(this, m2);
		}

	}

}
