package net.jadoth.persistence.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;


public interface PersistenceTypeLineage<T>
{
	public String typeName();
	
	public Class<T> runtimeType();
	
	public XGettingTable<Long, PersistenceTypeDefinition<T>> entries();
	
	public PersistenceTypeDefinition<T> latest();
	
	public PersistenceTypeDefinition<T> runtimeDefinition();
	
	public boolean registerTypeDefinition(PersistenceTypeDefinition<T> typeDefinition);
	
	

	public boolean setRuntimeTypeDefinition(PersistenceTypeDefinition<T> runtimeDefinition);
	
	
	
	public static <T> PersistenceTypeLineage.Implementation<T> New(
		final String   typeName   ,
		final Class<T> runtimeType
	)
	{
		return new PersistenceTypeLineage.Implementation<>(
			notNull(typeName)             , // may never be null as this is the lineage's identity.
			mayNull(runtimeType)            // can be null if the type cannot be resolved into a runtime class.
		);
	}
		
	public final class Implementation<T> implements PersistenceTypeLineage<T>
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                          typeName         ;
		final Class<T>                                        runtimeType      ;
		final EqHashTable<Long, PersistenceTypeDefinition<T>> entries          ;
		      PersistenceTypeDefinition<T>                    runtimeDefinition; // initialized effectively final



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final String typeName, final Class<T> runtimeType)
		{
			super();
			this.typeName    = typeName         ;
			this.runtimeType = runtimeType      ;
			this.entries     = EqHashTable.New();
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
		
		private void validate(final PersistenceTypeDefinition<T> typeDefinition)
		{
			if(this.isValid(typeDefinition))
			{
				return;
			}
			
			// (12.10.2017 TM)EXCP: proper exception
			throw new RuntimeException("Invalid type definition for type lineage " + this.typeName);
		}
		
		private boolean isValid(final PersistenceTypeDefinition<T> typeDefinition)
		{
			if(!this.typeName.equals(typeDefinition.typeName()))
			{
				return false;
			}
			if(this.runtimeType != typeDefinition.type())
			{
				return false;
			}
			
			final PersistenceTypeDefinition<T> alreadyRegistered = this.entries.get(typeDefinition.typeId());
			if(alreadyRegistered == null)
			{
				return true;
			}
			
			return PersistenceTypeDescriptionMember.equalMembers(
				typeDefinition.members(),
				alreadyRegistered.members()
			);
		}
		
		@Override
		public boolean registerTypeDefinition(final PersistenceTypeDefinition<T> typeDefinition)
		{
			this.validate(typeDefinition);
			return this.internalRegisterTypeDefinition(typeDefinition);
		}
		
		private boolean internalRegisterTypeDefinition(final PersistenceTypeDefinition<T> typeDefinition)
		{
			synchronized(this.entries)
			{
				// the passed (and already validated) instance gets set in any way, ...
				if(this.entries.put(typeDefinition.typeId(), typeDefinition))
				{
					// ... but the return value is only rue to indicate an actual additional entry.
					this.entries.keys().sort(Long::compare);
					return true;
				}
				
				// the definition was already there (and in order), only the instance has been replaced.
				return false;
			}
		}
				
		@Override
		public final boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition<T> runtimeDefinition)
		{
			synchronized(this.entries)
			{
				// false indicates no-op, actual non-viability causes exceptions
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
				
				// conflicting call/usage (runtime types and thus definitions are assumed to be immutable for now)
				// (26.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("Runtime definition already initialized");
			}
			
			if(this.isValid(runtimeDefinition))
			{
				return true;
			}
			
			throw new RuntimeException(
				"Invalid runtime definition for " + this.typeName() + " with type id: " + runtimeDefinition.typeId()
			);
		}
		
	}

}
