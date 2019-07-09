package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import one.microstream.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberPseudoFieldComplex
extends PersistenceTypeDefinitionMemberPseudoFieldVariableLength, PersistenceTypeDescriptionMemberFieldGenericComplex
{
	@Override
	public PersistenceTypeDefinitionMemberPseudoFieldComplex copyForName(String name);
	
	
	
	public static PersistenceTypeDefinitionMemberPseudoFieldComplex New(
		final PersistenceTypeDescriptionMemberFieldGenericComplex description
	)
	{
		return New(
			description.qualifier()              ,
			description.name()                   ,
			description.members()                ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldComplex New(
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldComplex New(
		final String                                                        qualifier              ,
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericComplex.Default
	implements PersistenceTypeDefinitionMemberPseudoFieldComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String                                                        qualifier              ,
			final String                                                        name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                          persistentMinimumLength,
			final long                                                          persistentMaximumLength
		)
		{
			super(
				qualifier              ,
				name                   ,
				members                ,
				persistentMinimumLength,
				persistentMaximumLength
			);
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
		public PersistenceTypeDefinitionMemberPseudoFieldComplex copyForName(
			final String qualifier,
			final String name
		)
		{
			return new PersistenceTypeDefinitionMemberPseudoFieldComplex.Default(
				qualifier,
				name,
				this.members(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}
		
		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldComplex copyForName(final String name)
		{
			return this.copyForName(null, name);
		}

	}

}
