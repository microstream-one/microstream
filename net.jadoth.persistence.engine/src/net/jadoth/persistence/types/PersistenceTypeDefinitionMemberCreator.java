package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.swizzling.types.Swizzle;

public interface PersistenceTypeDefinitionMemberCreator<M extends PersistenceTypeDefinitionMember>
{
	public M createDefinitionMember(PersistenceTypeDescriptionMemberPrimitiveDefinition description);
	
	public M createDefinitionMember(PersistenceTypeDescriptionMemberField description);
	
	public M createDefinitionMember(PersistenceTypeDescriptionMemberPseudoFieldSimple description);
	
	public M createDefinitionMember(PersistenceTypeDescriptionMemberPseudoFieldVariableLength description);
	
	public M createDefinitionMember(PersistenceTypeDescriptionMemberPseudoFieldComplex description);
	
	
	
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
	
	public final class Implementation
	implements PersistenceTypeDefinitionMemberCreator<PersistenceTypeDefinitionMember.EffectiveFinalOwnerTypeHolder>
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
		public PersistenceTypeDefinitionMemberPrimitiveDefinition.Implementation createDefinitionMember(
			final PersistenceTypeDescriptionMemberPrimitiveDefinition description
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDefinitionMemberCreator#createDefinitionMember()
		}
		
		private long determineHighestTypeId(
			final Iterable<PersistenceTypeDescription> ascendingOrderTypeIdEntries,
			final long                                 upperBoundTypeId           ,
			final String                               typeName
		)
		{
			long highestFoundTypeId = Swizzle.nullId();
			
			for(final PersistenceTypeDescription entry : ascendingOrderTypeIdEntries)
			{
				if(entry.typeId() >= upperBoundTypeId)
				{
					break;
				}
				if(entry.typeName().equals(typeName))
				{
					highestFoundTypeId = entry.typeId();
				}
			}
			
			return highestFoundTypeId;
		}

		@Override
		public PersistenceTypeDefinitionMemberField.Implementation createDefinitionMember(
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
		public PersistenceTypeDefinitionMemberPseudoFieldSimple.Implementation createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldSimple description
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDefinitionMemberCreator#createDefinitionMember()
		}

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Implementation createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldVariableLength description
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDefinitionMemberCreator#createDefinitionMember()
		}
		
		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldComplex.Implementation createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldComplex description
		)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDefinitionMemberCreator#createDefinitionMember()
		}
		
	}
	
}
