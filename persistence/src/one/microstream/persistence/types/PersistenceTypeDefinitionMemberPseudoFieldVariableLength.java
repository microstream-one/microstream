package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDefinitionMemberPseudoFieldVariableLength
extends PersistenceTypeDefinitionMemberPseudoField, PersistenceTypeDescriptionMemberPseudoFieldVariableLength
{
	@Override
	public PersistenceTypeDefinitionMemberPseudoFieldVariableLength copyForName(String name);
	
	

	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default New(
		final PersistenceTypeDescriptionMemberPseudoFieldVariableLength description
	)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.New(
			description.typeName()               ,
			description.name()                   ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(typeName, null, name, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default New(
		final String typeName               ,
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default(
			 notNull(typeName)               ,
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			         false                   ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default Bytes(
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return Bytes(
			null                   ,
			name                   ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default Bytes(
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(
			PersistenceTypeDictionary.Symbols.TYPE_BYTES,
			qualifier,
			name,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}

	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default Chars(
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return Chars(null, name, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default Chars(
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(
			PersistenceTypeDictionary.Symbols.TYPE_CHARS,
			qualifier              ,
			name                   ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}



	public class Default
	extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Default
	implements PersistenceTypeDefinitionMemberPseudoFieldVariableLength
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
			super(typeName, qualifier, name, hasReferences, persistentMinimumLength, persistentMaximumLength);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			return null;
		}

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldVariableLength copyForName(
			final String qualifier,
			final String name
		)
		{
			return new PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Default(
				this.typeName(),
				qualifier,
				name,
				this.isReference(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}
		
		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldVariableLength copyForName(final String name)
		{
			return this.copyForName(null, name);
		}

	}

}
