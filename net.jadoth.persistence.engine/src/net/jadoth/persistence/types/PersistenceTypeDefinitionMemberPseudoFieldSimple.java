package net.jadoth.persistence.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

public interface PersistenceTypeDefinitionMemberPseudoFieldSimple
extends PersistenceTypeDefinitionMemberPseudoField, PersistenceTypeDescriptionMemberPseudoFieldSimple
{
	public static PersistenceTypeDefinitionMemberPseudoFieldSimple New(
		final String   name                   ,
		final String   typeName               ,
		final Class<?> type                   ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldSimple.Implementation(
			 notNull(name)                   ,
			 notNull(typeName)               ,
			 mayNull(type)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Implementation
	extends PersistenceTypeDescriptionMemberPseudoField.AbstractImplementation
	implements PersistenceTypeDefinitionMemberPseudoFieldSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
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
