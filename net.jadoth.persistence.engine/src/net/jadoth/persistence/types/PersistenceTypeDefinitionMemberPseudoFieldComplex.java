package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberPseudoFieldComplex<O>
extends PersistenceTypeDefinitionMemberPseudoFieldVariableLength<O>, PersistenceTypeDescriptionMemberPseudoFieldComplex
{
	
	public final class Implementation<O>
	extends PersistenceTypeDescriptionMemberPseudoFieldComplex.Implementation
	implements
	PersistenceTypeDefinitionMemberPseudoFieldComplex<O>,
	PersistenceTypeDefinitionMember.EffectiveFinalOwnerTypeHolder<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private /*f*/ PersistenceTypeDefinition<O> ownerType;
		
		

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
		public final Class<?> type()
		{
			return null;
		}

	}

}
