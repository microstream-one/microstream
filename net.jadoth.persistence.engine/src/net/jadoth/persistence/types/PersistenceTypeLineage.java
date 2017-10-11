package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;

public interface PersistenceTypeLineage<T>
{
	public String typeName();
	
	public Class<T> runtimeType();
	
	public XGettingTable<Long, PersistenceTypeDefinition<T>> entries();
	
	public PersistenceTypeDefinition<T> latest();
	
	public PersistenceTypeDefinition<T> runtimeDefinition();
		
	public boolean registerTypeDescription(long typeId, XGettingSequence<? extends PersistenceTypeDescriptionMember> members);

	public boolean initializeRuntimeTypeDefinition(PersistenceTypeDefinition<T> runtimeDefinition);
	
	
	
	public static <T> PersistenceTypeLineage.Implementation<T> New(
		final String                           typeName             ,
		final Class<T>                         runtimeType          ,
		final PersistenceTypeDefinitionCreator typeDefinitionCreator
	)
	{
		return new PersistenceTypeLineage.Implementation<>(
			notNull(typeName)             , // may never be null as this is the lineage's identity.
			runtimeType                   , // can be null if the type cannot be resolved into a runtime class.
			notNull(typeDefinitionCreator)
		);
	}
		
	public final class Implementation<T> implements PersistenceTypeLineage<T>
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                          typeName             ;
		final Class<T>                                        runtimeType          ;
		final EqHashTable<Long, PersistenceTypeDefinition<T>> entries              ;
		final PersistenceTypeDefinitionCreator                typeDefinitionCreator;
		      PersistenceTypeDefinition<T>                    runtimeDefinition   ; // initialized effectively final



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String                           typeName             ,
			final Class<T>                         runtimeType          ,
			final PersistenceTypeDefinitionCreator typeDefinitionCreator
		)
		{
			super();
			this.typeName              = typeName             ;
			this.runtimeType           = runtimeType          ;
			this.entries               = EqHashTable.New()    ;
			this.typeDefinitionCreator = typeDefinitionCreator;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String typeName()
		{
			return this.typeName;
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition<T>> entries()
		{
			return this.entries;
		}

		@Override
		public final Class<T> runtimeType()
		{
			return this.runtimeType;
		}

		@Override
		public final PersistenceTypeDefinition<T> runtimeDefinition()
		{
			return this.runtimeDefinition;
		}
		
		@Override
		public final PersistenceTypeDefinition<T> latest()
		{
			synchronized(this.entries)
			{
				return this.entries.values().peek();
			}
		}
		
		@Override
		public final boolean registerTypeDescription(
			final long                                                         typeId ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			final PersistenceTypeDefinition<T> typeDesciption = this.typeDefinitionCreator.createTypeDefinition(
				this.typeName(),
				this.runtimeType(),
				typeId,
				members
			);
			
			synchronized(this.entries)
			{
				if(this.entries.add(typeId, typeDesciption))
				{
					this.entries.keys().sort(Long::compare);
					return true;
				}
				return false;
			}
		}
		
		@Override
		public final boolean initializeRuntimeTypeDefinition(final PersistenceTypeDefinition<T> runtimeDefinition)
		{
			synchronized(this.entries)
			{
				// true indicates no-op, actual non-viability causes exceptions
				if(!this.checkViability(runtimeDefinition))
				{
					return false;
				}
				
				// normal case: effective final initialization
				this.runtimeDefinition = runtimeDefinition;
				
				// correct behavior of the put has been checked above
				this.entries.put(runtimeDefinition.typeId(), runtimeDefinition);
				
				return true;
			}
		}
		
		private boolean checkViability(final PersistenceTypeDefinition<T> runtimeDefinition)
		{
			if(this.runtimeDefinition != null)
			{
				if(this.runtimeDefinition == runtimeDefinition)
				{
					// no-op call, abort
					return false;
				}
				
				// conflicting call/usage
				// (26.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("Runtime definition already initialized");
			}
			
			final Long                         typeId     = runtimeDefinition.typeId();
			final PersistenceTypeDefinition<T> latest     = this.latest();
			final PersistenceTypeDefinition<T> equivalent = this.entries.get(typeId);
			if(equivalent != null && equivalent != latest)
			{
				// (28.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("Invalid runtime definition for type id: " + typeId);
			}
						
			return true;
		}
		
	}

}
