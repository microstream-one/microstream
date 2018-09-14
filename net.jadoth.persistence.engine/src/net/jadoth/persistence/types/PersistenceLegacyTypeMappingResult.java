package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;


public interface PersistenceLegacyTypeMappingResult<M, T>
{
	/* (14.09.2018 TM)FIXME: OGS-3: Single T for two mapped types?
	 * Which "T" is it? Source or target? Can there be problems?
	 * Research and change or comment.
	 */
	
	public PersistenceTypeDefinition<T> legacyTypeDefinition();
	
	public PersistenceTypeHandler<M, T> currentTypeHandler();
	
	public XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers();
	
	public XGettingSet<PersistenceTypeDescriptionMember> newCurrentMembers();
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> New(
		final PersistenceTypeDefinition<T>                                                    legacyTypeDefinition  ,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers,
		final XGettingSet<PersistenceTypeDescriptionMember>                                   newCurrentMembers
	)
	{
		return new PersistenceLegacyTypeMappingResult.Implementation<>(
			notNull(legacyTypeDefinition)  ,
			notNull(currentTypeHandler)    ,
			notNull(legacyToCurrentMembers),
			notNull(newCurrentMembers)
		);
	}
	
	public final class Implementation<M, T> implements PersistenceLegacyTypeMappingResult<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDefinition<T>                                               legacyTypeDefinition;
		private final PersistenceTypeHandler<M, T>                                               currentTypeHandler  ;
		private final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers;
		private final XGettingSet<PersistenceTypeDescriptionMember>                              newCurrentMembers   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDefinition<T>                                                    legacyTypeDefinition  ,
			final PersistenceTypeHandler<M, T>                                                    currentTypeHandler    ,
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers,
			final XGettingSet<PersistenceTypeDescriptionMember>                                   newCurrentMembers
		)
		{
			super();
			this.legacyTypeDefinition   = legacyTypeDefinition  ;
			this.currentTypeHandler     = currentTypeHandler    ;
			this.legacyToCurrentMembers = legacyToCurrentMembers;
			this.newCurrentMembers      = newCurrentMembers     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDefinition<T> legacyTypeDefinition()
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
		public XGettingSet<PersistenceTypeDescriptionMember> newCurrentMembers()
		{
			return this.newCurrentMembers;
		}
		
	}
	
}
