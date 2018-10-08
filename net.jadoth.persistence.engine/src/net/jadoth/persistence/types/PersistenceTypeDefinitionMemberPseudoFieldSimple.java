package net.jadoth.persistence.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

public interface PersistenceTypeDefinitionMemberPseudoFieldSimple<O>
extends PersistenceTypeDefinitionMemberPseudoField<O>, PersistenceTypeDescriptionMemberPseudoFieldSimple
{
	public static <O> PersistenceTypeDefinitionMemberPseudoFieldSimple.Implementation<O> New(
		final String   name                   ,
		final String   typeName               ,
		final Class<?> type                   ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldSimple.Implementation<>(
			 notNull(name)                   ,
			 notNull(typeName)               ,
			 mayNull(type)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public final class Implementation<O>
	extends PersistenceTypeDescriptionMemberPseudoField.AbstractImplementation
	implements
	PersistenceTypeDefinitionMemberPseudoFieldSimple<O>,
	PersistenceTypeDefinitionMember.EffectiveFinalOwnerTypeHolder<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private final Class<?>                     type     ;
		private /*f*/ PersistenceTypeDefinition<O> ownerType;
		
		
		
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
			super(typeName, name, isReference, !isReference, isReference, persistentMinLength, persistentMaxLength);
			this.type = type;
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
			return this.type;
		}

		@Override
		public void assembleTypeDescription(final Appender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
