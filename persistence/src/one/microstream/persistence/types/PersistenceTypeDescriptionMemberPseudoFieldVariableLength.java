package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberPseudoFieldVariableLength
extends PersistenceTypeDescriptionMemberFieldGeneric
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
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
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
		return New(typeName, null, name, persistentMinimumLength, persistentMaximumLength);
	}

	public static PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Default New(
		final String typeName               ,
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Default(
			 notNull(typeName),
			 mayNull(qualifier),
			 notNull(name),
			         false,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDescriptionMemberPseudoFieldVariableLength
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName               ,
			final String  qualifier              ,
			final String  name                   ,
			final boolean hasReferences          ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			super(
				typeName               ,
				qualifier              ,
				name                   ,
				false                  ,
				false                  ,
				hasReferences          ,
				persistentMinimumLength,
				persistentMaximumLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
