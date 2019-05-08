package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.util.similarity.Similarity;


public interface PersistenceLegacyTypeMappingResult<M, T>
{
	// the legacy type might potentially or usually be another type, maybe one that no more has a runtime type.
	public PersistenceTypeDefinition legacyTypeDefinition();
	
	public PersistenceTypeHandler<M, T> currentTypeHandler();
	
	public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers();
	
	public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers();

	public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers();
	
	public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers();
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> New(
		final PersistenceTypeDefinition                                                                   legacyTypeDefinition  ,
		final PersistenceTypeHandler<M, T>                                                                currentTypeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers
	)
	{
		return new PersistenceLegacyTypeMappingResult.Default<>(
			notNull(legacyTypeDefinition)  ,
			notNull(currentTypeHandler)    ,
			notNull(legacyToCurrentMembers),
			notNull(currentToLegacyMembers),
			notNull(discardedLegacyMembers),
			notNull(newCurrentMembers)
		);
	}
	
	public final class Default<M, T> implements PersistenceLegacyTypeMappingResult<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinition                     legacyTypeDefinition  ;
		final PersistenceTypeHandler<M, T>                  currentTypeHandler    ;
		final XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers     ;
		
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
			legacyToCurrentMembers,
			currentToLegacyMembers
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeDefinition                                                                   legacyTypeDefinition  ,
			final PersistenceTypeHandler<M, T>                                                                currentTypeHandler    ,
			final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
			final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers
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
		public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers()
		{
			return this.legacyToCurrentMembers;
		}
		
		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers()
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
