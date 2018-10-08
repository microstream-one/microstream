package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberPseudoFieldSimple
extends PersistenceTypeDescriptionMemberPseudoField
{
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		// the type check is the only specific thing here.
		return member instanceof PersistenceTypeDescriptionMemberPseudoFieldSimple
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPseudoFieldSimple)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPseudoFieldSimple m1,
		final PersistenceTypeDescriptionMemberPseudoFieldSimple m2
	)
	{
		// currently no specific checking logic
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberPseudoFieldSimple createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	
	public static PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation New(
		final String   name                   ,
		final String   typeName               ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation(
			 notNull(name)                   ,
			 notNull(typeName)               ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public final class Implementation
	extends PersistenceTypeDescriptionMemberPseudoField.AbstractImplementation
	implements PersistenceTypeDescriptionMemberPseudoFieldSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String  name               ,
			final String  typeName           ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(typeName, name, isReference, !isReference, isReference, persistentMinLength, persistentMaxLength);
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
