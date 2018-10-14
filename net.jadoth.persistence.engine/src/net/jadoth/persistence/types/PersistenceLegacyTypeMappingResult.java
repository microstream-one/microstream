package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;


public interface PersistenceLegacyTypeMappingResult<M, T>
{
	// the legacy type might potentially or usually be another type, maybe one that no more has a runtime type.
	public PersistenceTypeDefinition legacyTypeDefinition();
	
	public PersistenceTypeHandler<M, T> currentTypeHandler();
	
	public XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers();
	
	public XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers();

	public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers();
	
	public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers();
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> New(
		final PersistenceTypeDefinition                                                       legacyTypeDefinition  ,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers,
		final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                   discardedLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                   newCurrentMembers
	)
	{
		return new PersistenceLegacyTypeMappingResult.Implementation<>(
			notNull(legacyTypeDefinition)  ,
			notNull(currentTypeHandler)    ,
			notNull(legacyToCurrentMembers),
			notNull(currentToLegacyMembers),
			notNull(discardedLegacyMembers),
			notNull(newCurrentMembers)
		);
	}
	
	public final class Implementation<M, T> implements PersistenceLegacyTypeMappingResult<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinition                     legacyTypeDefinition  ;
		final PersistenceTypeHandler<M, T>                  currentTypeHandler    ;
		final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers;
		final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers;
		final XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDefinition                                                       legacyTypeDefinition  ,
			final PersistenceTypeHandler<M, T>                                                    currentTypeHandler    ,
			final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers,
			final XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                   discardedLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                   newCurrentMembers
		)
		{
			super();
			this.legacyTypeDefinition   = legacyTypeDefinition  ;
			this.currentTypeHandler     = currentTypeHandler    ;
			this.legacyToCurrentMembers = legacyToCurrentMembers;
			this.currentToLegacyMembers = currentToLegacyMembers;
			this.discardedLegacyMembers = discardedLegacyMembers;
			this.newCurrentMembers      = newCurrentMembers     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDefinition legacyTypeDefinition()
		{
			return this.legacyTypeDefinition;
		}

		@Override
		public PersistenceTypeHandler<M, T> currentTypeHandler()
		{
			return this.currentTypeHandler;
		}

		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers()
		{
			return this.legacyToCurrentMembers;
		}
		
		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers()
		{
			return this.currentToLegacyMembers;
		}
		
		@Override
		public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers()
		{
			return this.discardedLegacyMembers;
		}

		@Override
		public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers()
		{
			return this.newCurrentMembers;
		}
		
	}
	
}
