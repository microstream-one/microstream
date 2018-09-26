package net.jadoth.persistence.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

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
	
	public static PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation New(
		final String   name                   ,
		final Class<?> type                   ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation(
			notNull(name)          ,
			type.getName()         ,
			notNull(type)          ,
			!type.isPrimitive()    ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}
	
	public static PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation New(
		final String   name                   ,
		final Class<?> optionalType           ,
		final String   typeName               ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberPseudoFieldSimple.Implementation(
			notNull(name)          ,
			notNull(typeName)      ,
			mayNull(optionalType)  ,
			isReference            ,
			persistentMinimumLength,
			persistentMaximumLength
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
			final String   name               ,
			final String   typeName           ,
			final Class<?> type               ,
			final boolean  isReference        ,
			final long     persistentMinLength,
			final long     persistentMaxLength
		)
		{
			super(type, typeName, name, isReference, !isReference, isReference, persistentMinLength, persistentMaxLength);
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
