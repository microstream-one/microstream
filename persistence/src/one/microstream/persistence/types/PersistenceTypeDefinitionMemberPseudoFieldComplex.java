package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import one.microstream.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberPseudoFieldComplex
extends PersistenceTypeDefinitionMemberPseudoFieldVariableLength, PersistenceTypeDescriptionMemberPseudoFieldComplex
{
	@Override
	public PersistenceTypeDefinitionMemberPseudoFieldComplex copyForName(String name);
	
	
	
	public static PersistenceTypeDefinitionMemberPseudoFieldComplex New(
		final PersistenceTypeDescriptionMemberPseudoFieldComplex description
	)
	{
		return New(
			description.name()                   ,
			description.members()                ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	public static PersistenceTypeDefinitionMemberPseudoFieldComplex New(
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPseudoFieldComplex.Implementation(
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Implementation
	extends PersistenceTypeDescriptionMemberPseudoFieldComplex.Implementation
	implements PersistenceTypeDefinitionMemberPseudoFieldComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final String                                                        name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members                ,
			final long                                                          persistentMinimumLength,
			final long                                                          persistentMaximumLength
		)
		{
			super(
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
		public PersistenceTypeDefinitionMemberPseudoFieldComplex copyForName(final String name)
		{
			return new PersistenceTypeDefinitionMemberPseudoFieldComplex.Implementation(
				name,
				this.members(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
