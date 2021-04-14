package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDefinitionMemberFieldGenericSimple
extends PersistenceTypeDefinitionMemberFieldGeneric, PersistenceTypeDescriptionMemberFieldGenericSimple
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericSimple copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}
	
	@Override
	public PersistenceTypeDefinitionMemberFieldGenericSimple copyForName(String qualifier, String name);
	
	
	
	public static PersistenceTypeDefinitionMemberFieldGenericSimple New(
		final String   typeName               ,
		final String   qualifier              ,
		final String   name                   ,
		final Class<?> type                   ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldGenericSimple.Default(
			 notNull(typeName)               ,
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 mayNull(type)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDefinitionMemberFieldGenericSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String   typeName           ,
			final String   qualifier          ,
			final String   name               ,
			final Class<?> type               ,
			final boolean  isReference        ,
			final long     persistentMinLength,
			final long     persistentMaxLength
		)
		{
			super(typeName, qualifier, name, isReference, !isReference, isReference, persistentMinLength, persistentMaxLength);
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
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

		@Override
		public PersistenceTypeDefinitionMemberFieldGenericSimple copyForName(final String qualifier, final String name)
		{
			return new PersistenceTypeDefinitionMemberFieldGenericSimple.Default(
				this.typeName()               ,
				qualifier                     ,
				name				          ,
				this.type                     ,
				this.isReference()            ,
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
