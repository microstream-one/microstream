package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

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



	public static PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Default New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Default(
			 notNull(typeName),
			 notNull(name),
			         false,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberPseudoField.Abstract
	implements PersistenceTypeDescriptionMemberPseudoFieldVariableLength
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
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
