package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;
import static net.jadoth.math.XMath.positive;

import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberPseudoFieldComplex
extends PersistenceTypeDefinitionMemberPseudoFieldVariableLength, PersistenceTypeDescriptionMemberPseudoFieldComplex
{
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

	}

}
