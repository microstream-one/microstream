package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

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
			 notNull(typeName),
			 notNull(name),
			         false,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Implementation
	extends PersistenceTypeDescriptionMemberPseudoField.AbstractImplementation
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
			super(typeName, name, false, false, hasReferences, persistentMinimumLength, persistentMaximumLength);
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
