package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;

public interface PersistenceTypeDescriptionMemberFieldGenericComplex
extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength
{
	public XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members();

	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& equalDescription(this, (PersistenceTypeDescriptionMemberFieldGenericComplex)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2)
			&& PersistenceTypeDescriptionMember.equalDescriptions(m1.members(), m2.members())
		;
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberPseudoFieldComplex createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                        qualifier              ,
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberPseudoFieldVariableLength.Default
	implements PersistenceTypeDescriptionMemberFieldGenericComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableSequence<PersistenceTypeDescriptionMemberFieldGeneric> members;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                        qualifier              ,
			final String                                                        name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                          persistentMinimumLength,
			final long                                                          persistentMaximumLength
		)
		{
			super(
				PersistenceTypeDictionary.Symbols.TYPE_COMPLEX,
				qualifier,
				name,
				PersistenceTypeDescriptionMember.determineHasReferences(members),
				persistentMinimumLength,
				persistentMaximumLength
			);
			this.members = members.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members()
		{
			return this.members;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
