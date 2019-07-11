package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldGenericVariableLength
extends PersistenceTypeDescriptionMemberFieldGeneric
{
	@Override
	public default boolean isVariableLength()
	{
		return true;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		// the type check is the only specific thing here.
		return other instanceof PersistenceTypeDescriptionMemberFieldGenericVariableLength
			&& PersistenceTypeDescriptionMemberFieldGeneric.super.equalsStructure(other)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m1,
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m1,
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericVariableLength createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	

	public static PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(typeName, null, name, persistentMinimumLength, persistentMaximumLength);
	}

	public static PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default(
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
	implements PersistenceTypeDescriptionMemberFieldGenericVariableLength
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
