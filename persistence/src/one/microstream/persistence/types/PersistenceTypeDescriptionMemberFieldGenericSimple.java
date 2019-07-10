package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldGenericSimple
extends PersistenceTypeDescriptionMemberFieldGeneric
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericSimple createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	public static PersistenceTypeDescriptionMemberFieldGenericSimple.Default New(
		final String  typeName               ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return New(typeName, null, name, isReference, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericSimple.Default New(
		final String  typeName               ,
		final String  qualifier              ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericSimple.Default(
			 notNull(typeName)               ,
			 notNull(qualifier)              ,
			 notNull(name)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public final class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDescriptionMemberFieldGenericSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName           ,
			final String  qualifier          ,
			final String  name               ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName,
				qualifier,
				name,
				isReference,
				!isReference,
				isReference,
				persistentMinLength,
				persistentMaxLength
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
