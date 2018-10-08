package net.jadoth.persistence.types;

public interface PersistenceTypeDescriptionMemberPseudoFieldVariableLength
extends PersistenceTypeDescriptionMemberPseudoField
{
	@Override
	public default boolean isVariableLength()
	{
		return true;
	}
	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		// the type check is the only specific thing here.
		return member instanceof PersistenceTypeDescriptionMemberPseudoFieldVariableLength
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPseudoFieldVariableLength)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPseudoFieldVariableLength m1,
		final PersistenceTypeDescriptionMemberPseudoFieldVariableLength m2
	)
	{
		// currently no specific checking logic
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberPseudoFieldVariableLength createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}



	public static PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Implementation New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Implementation(
			typeName,
			name,
			false,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}


	public static PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Implementation Bytes(
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

	public static PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Implementation Chars(
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



	public class Implementation
	extends PersistenceTypeDescriptionMember.AbstractImplementation
	implements PersistenceTypeDescriptionMemberPseudoFieldVariableLength
	{
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
			super(typeName, name, false, false, false, hasReferences, persistentMinimumLength, persistentMaximumLength);
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
