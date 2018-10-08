package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;


public interface PersistenceLegacyTypeMappingResult<M, T>
{
	// the legacy type might potentially or usually be another type, maybe one that no more has a runtime type.
	public PersistenceTypeDefinition legacyTypeDefinition();
	
	public PersistenceTypeHandler<M, T> currentTypeHandler();
	
	public XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers();

	public XGettingSet<PersistenceTypeDefinitionMember> deletedLegacyMembers();
	
	public XGettingSet<PersistenceTypeDefinitionMember> newCurrentMembers();
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> New(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition  ,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  deletedLegacyMembers  ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  newCurrentMembers
	)
	{
		return new PersistenceLegacyTypeMappingResult.Implementation<>(
			notNull(legacyTypeDefinition)  ,
			notNull(currentTypeHandler)    ,
			notNull(legacyToCurrentMembers),
			notNull(deletedLegacyMembers)  ,
			notNull(newCurrentMembers)
		);
	}
	
	public final class Implementation<M, T> implements PersistenceLegacyTypeMappingResult<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDefinition                    legacyTypeDefinition;
		private final PersistenceTypeHandler<M, T>                 currentTypeHandler  ;
		private final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers;
		private final XGettingSet<PersistenceTypeDefinitionMember> deletedLegacyMembers;
		private final XGettingSet<PersistenceTypeDefinitionMember> newCurrentMembers   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDefinition                                                     legacyTypeDefinition  ,
			final PersistenceTypeHandler<M, T>                                                  currentTypeHandler    ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers,
			final XGettingSet<PersistenceTypeDefinitionMember>                                  deletedLegacyMembers  ,
			final XGettingSet<PersistenceTypeDefinitionMember>                                  newCurrentMembers
		)
		{
			super();
			this.legacyTypeDefinition   = legacyTypeDefinition  ;
			this.currentTypeHandler     = currentTypeHandler    ;
			this.legacyToCurrentMembers = legacyToCurrentMembers;
			this.deletedLegacyMembers   = deletedLegacyMembers  ;
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
		public XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers()
		{
			return this.legacyToCurrentMembers;
		}
		
		@Override
		public XGettingSet<PersistenceTypeDefinitionMember> deletedLegacyMembers()
		{
			return this.deletedLegacyMembers;
		}

		@Override
		public XGettingSet<PersistenceTypeDefinitionMember> newCurrentMembers()
		{
			return this.newCurrentMembers;
		}
		
	}
	
}
