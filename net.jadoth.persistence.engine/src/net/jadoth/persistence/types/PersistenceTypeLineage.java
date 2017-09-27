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

	public void initializeRuntimeTypeDescription(PersistenceTypeDefinition<T> runtimeDescription);
	
	
	public static <T> PersistenceTypeLineage.Implementation<T> New(
		final String                           typeName             ,
		final PersistenceTypeDefinitionBuilder typeDefinitionBuilder,
		final Class<T>                         runtimeType
	)
	{
		return new PersistenceTypeLineage.Implementation<>(
			notNull(typeName)             , // may never be null as this is the lineage's identity.
			notNull(typeDefinitionBuilder),
			runtimeType                     // can be null if the type cannot be resolved into a runtime class.
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
		final PersistenceTypeDefinitionBuilder                typeDefinitionBuilder;
		      PersistenceTypeDefinition<T>                    runtimeDescription   ; // initialized effectively final



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String                           typeName             ,
			final PersistenceTypeDefinitionBuilder typeDefinitionBuilder,
			final Class<T>                         runtimeType
		)
		{
			super();
			this.typeName              = typeName             ;
			this.entries               = EqHashTable.New()    ;
			this.typeDefinitionBuilder = typeDefinitionBuilder;
			this.runtimeType           = runtimeType          ;
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
			return this.runtimeDescription;
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
			final PersistenceTypeDefinition<T> typeDesciption = this.typeDefinitionBuilder.build(this, typeId, members);
			
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
		public final void initializeRuntimeTypeDescription(final PersistenceTypeDefinition<T> runtimeDescription)
		{
			synchronized(this.entries)
			{
				if(this.runtimeDescription != null)
				{
					if(this.runtimeDescription == runtimeDescription)
					{
						// no-op call, abort
						return;
					}
					
					// conflicting call/usage
					throw new RuntimeException("Runtime Description already initialized"); // (26.09.2017 TM)EXCP: proper exception
				}
				
				// normal case: effective final initialization
				this.runtimeDescription = runtimeDescription;
				
				// must result in the typeId being the highest in any case.
				this.entries.put(runtimeDescription.typeId(), runtimeDescription);
			}
		}
		
	}

}
