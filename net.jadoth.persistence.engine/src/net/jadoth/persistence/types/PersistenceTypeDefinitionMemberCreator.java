package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

public interface PersistenceTypeDefinitionMemberCreator
{
	public PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
		PersistenceTypeDescriptionMemberPrimitiveDefinition description
	);
	
	public PersistenceTypeDefinitionMemberField createDefinitionMember(
		PersistenceTypeDescriptionMemberField description
	);
	
	public PersistenceTypeDefinitionMemberPseudoFieldSimple createDefinitionMember(
		PersistenceTypeDescriptionMemberPseudoFieldSimple description
	);
	
	public PersistenceTypeDefinitionMemberPseudoFieldVariableLength createDefinitionMember(
		PersistenceTypeDescriptionMemberPseudoFieldVariableLength description
	);
	
	public PersistenceTypeDefinitionMemberPseudoFieldComplex createDefinitionMember(
		PersistenceTypeDescriptionMemberPseudoFieldComplex description
	);
	
	
	
	public static PersistenceTypeDefinitionMemberCreator.Implementation New(
		final Iterable<? extends PersistenceTypeDescription> ascendingOrderTypeIdEntries,
		final PersistenceTypeDescription                     ownerType                  ,
		final PersistenceTypeResolver                        typeResolver
	)
	{
		return new PersistenceTypeDefinitionMemberCreator.Implementation(
			notNull(ascendingOrderTypeIdEntries),
			notNull(ownerType)                  ,
			notNull(typeResolver)
		);
	}
	
	public final class Implementation implements PersistenceTypeDefinitionMemberCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Iterable<? extends PersistenceTypeDescription> ascendingOrderTypeIdEntries;
		private final PersistenceTypeDescription                     ownerType                  ;
		private final PersistenceTypeResolver                        typeResolver               ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final Iterable<? extends PersistenceTypeDescription> ascendingOrderTypeIdEntries,
			final PersistenceTypeDescription                     ownerType                  ,
			final PersistenceTypeResolver                        typeResolver
		)
		{
			super();
			this.ascendingOrderTypeIdEntries = ascendingOrderTypeIdEntries;
			this.ownerType                   = ownerType                  ;
			this.typeResolver                = typeResolver               ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
			final PersistenceTypeDescriptionMemberPrimitiveDefinition description
		)
		{
			return PersistenceTypeDefinitionMemberPrimitiveDefinition.New(description);
		}
		
		private PersistenceTypeDescription determineMostRecentType(
			final Iterable<PersistenceTypeDescription> ascendingOrderTypeIdEntries,
			final long                                 upperBoundTypeId           ,
			final String                               typeName
		)
		{
			PersistenceTypeDescription mostRecentType = null;
			
			for(final PersistenceTypeDescription entry : ascendingOrderTypeIdEntries)
			{
				if(entry.typeId() >= upperBoundTypeId)
				{
					break;
				}
				if(typeName.equals(entry.typeName()))
				{
					mostRecentType = entry;
				}
			}
			
			return mostRecentType;
		}

		@Override
		public PersistenceTypeDefinitionMemberField createDefinitionMember(
			final PersistenceTypeDescriptionMemberField description
		)
		{
			/* (08.10.2018 TM)FIXME: OGS-3: tricky declaring class resolution:
			 * 1.) determine the declaring class typeId using ascendingOrderTypeIdEntries
			 * 2.) resolve the declaring class current name using the typeResolver
			 * 3.) resolve tht declaring class current name to a runtime Class is possible
			 * 4.) construct a definition member field instance with the mapped-to-current declaring class
			 * uff
			 */
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDefinitionMemberCreator#createDefinitionMember()
		}

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldSimple createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldSimple description
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDefinitionMemberCreator#createDefinitionMember()
		}

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldVariableLength createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldVariableLength description
		)
		{
			return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.New(description);
		}
		
		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldComplex createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldComplex description
		)
		{
			return PersistenceTypeDefinitionMemberPseudoFieldComplex.New(description);
		}
		
	}
	
}
