package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;


public interface PersistenceLegacyTypeMappingResult<M, T>
{
	// the legacy type might potentially or usually be another type, maybe one that no more has a runtime type.
	public PersistenceTypeDefinition<?> legacyTypeDefinition();
	
	public PersistenceTypeHandler<M, T> currentTypeHandler();
	
	public XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers();

	public XGettingSet<PersistenceTypeDescriptionMember> deletedLegacyMembers();
	
	public XGettingSet<PersistenceTypeDescriptionMember> newCurrentMembers();
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> New(
		final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition  ,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers,
		final XGettingSet<PersistenceTypeDescriptionMember>                                   deletedLegacyMembers  ,
		final XGettingSet<PersistenceTypeDescriptionMember>                                   newCurrentMembers
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
		
		private final PersistenceTypeDefinition<?>                  legacyTypeDefinition;
		private final PersistenceTypeHandler<M, T>                  currentTypeHandler  ;
		private final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers;
		private final XGettingSet<PersistenceTypeDescriptionMember> deletedLegacyMembers;
		private final XGettingSet<PersistenceTypeDescriptionMember> newCurrentMembers   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition  ,
			final PersistenceTypeHandler<M, T>                                                    currentTypeHandler    ,
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers,
			final XGettingSet<PersistenceTypeDescriptionMember>                                   deletedLegacyMembers  ,
			final XGettingSet<PersistenceTypeDescriptionMember>                                   newCurrentMembers
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
		public PersistenceTypeDefinition<?> legacyTypeDefinition()
		{
			return this.legacyTypeDefinition;
		}

		@Override
		public PersistenceTypeHandler<M, T> currentTypeHandler()
		{
			return this.currentTypeHandler;
		}

		@Override
		public XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers()
		{
			return this.legacyToCurrentMembers;
		}
		
		@Override
		public XGettingSet<PersistenceTypeDescriptionMember> deletedLegacyMembers()
		{
			return this.deletedLegacyMembers;
		}

		@Override
		public XGettingSet<PersistenceTypeDescriptionMember> newCurrentMembers()
		{
			return this.newCurrentMembers;
		}
		
	}
	
}
