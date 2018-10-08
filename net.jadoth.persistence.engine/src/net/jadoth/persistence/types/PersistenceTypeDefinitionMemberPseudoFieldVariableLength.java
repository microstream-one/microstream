package net.jadoth.persistence.types;

public interface PersistenceTypeDefinitionMemberPseudoFieldVariableLength<O>
extends PersistenceTypeDefinitionMemberPseudoField<O>, PersistenceTypeDescriptionMemberPseudoFieldVariableLength
{
	public static <O> PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Implementation<O> New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Implementation<>(
			typeName               ,
			name                   ,
			false                  ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}


	public static <O> PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Implementation<O> Bytes(
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(
			PersistenceTypeDictionary.Symbols.TYPE_BYTES,
			name,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}

	public static <O> PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Implementation<O> Chars(
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(
			PersistenceTypeDictionary.Symbols.TYPE_CHARS,
			name,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}



	public class Implementation<O>
	extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Implementation
	implements
	PersistenceTypeDefinitionMemberPseudoFieldVariableLength<O>,
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

		Implementation(
			final String  typeName               ,
			final String  name                   ,
			final boolean hasReferences          ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			super(typeName, name, hasReferences, persistentMinimumLength, persistentMaximumLength);
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
