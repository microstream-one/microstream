package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberPseudoFieldComplex
extends PersistenceTypeDefinitionMemberPseudoFieldVariableLength, PersistenceTypeDescriptionMemberPseudoFieldComplex
{
	
	public final class Implementation
	extends PersistenceTypeDescriptionMemberPseudoFieldComplex.Implementation
	implements
	PersistenceTypeDefinitionMemberPseudoFieldComplex,
	PersistenceTypeDefinitionMember.EffectiveFinalOwnerTypeHolder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private /*f*/ PersistenceTypeDefinition ownerType;
		
		

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
				name                   ,
				members                ,
				persistentMinimumLength,
				persistentMaximumLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceTypeDefinition ownerType()
		{
			return this.ownerType;
		}
		
		@Override
		public final void internalSetValidatedOwnerType(final PersistenceTypeDefinition ownerType)
		{
			this.ownerType = ownerType;
		}
		
		@Override
		public final Class<?> type()
		{
			return null;
		}

	}

}
