package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDefinitionMemberPseudoFieldSimple
extends PersistenceTypeDefinitionMemberPseudoField, PersistenceTypeDescriptionMemberPseudoFieldSimple
{
	@Override
	public PersistenceTypeDefinitionMemberPseudoFieldSimple copyForName(String name);
	
	
	
	public static PersistenceTypeDefinitionMemberPseudoFieldSimple New(
		final String   name                   ,
		final String   typeName               ,
		final Class<?> type                   ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldSimple.Default(
			 notNull(name)                   ,
			 notNull(typeName)               ,
			 mayNull(type)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberPseudoField.Abstract
	implements PersistenceTypeDefinitionMemberPseudoFieldSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
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

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldSimple copyForName(final String name)
		{
			return new PersistenceTypeDefinitionMemberPseudoFieldSimple.Default(
				name,
				this.typeName(),
				this.type,
				this.isReference(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
