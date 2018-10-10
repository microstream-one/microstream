package net.jadoth.persistence.types;

import static net.jadoth.X.mayNull;

import net.jadoth.chars.XChars;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;


public interface PersistenceTypeLineage
{
	public String typeName();
	
	public Class<?> type();
	
	public XGettingTable<Long, PersistenceTypeDefinition> entries();
	
	public PersistenceTypeDefinition latest();
	
	public PersistenceTypeDefinition runtimeDefinition();
	
	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);
	
	

	public boolean setRuntimeTypeDefinition(PersistenceTypeDefinition runtimeDefinition);
	
	
	
	public static PersistenceTypeLineage.Implementation New(
		final String   runtimeTypeName,
		final Class<?> runtimeType
	)
	{
		return new PersistenceTypeLineage.Implementation(
			mayNull(runtimeTypeName), // can be null for types explicitely mapped as having no runtime type.
			mayNull(runtimeType)      // can be null if the type name cannot be resolved to a runtime class.
		);
	}
		
	public final class Implementation implements PersistenceTypeLineage
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                       runtimeTypeName  ;
		final Class<?>                                     runtimeType      ;
		final EqHashTable<Long, PersistenceTypeDefinition> entries          ;
		      PersistenceTypeDefinition                    runtimeDefinition; // initialized effectively final



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final String runtimeTypeName, final Class<?> runtimeType)
		{
			super();
			this.runtimeTypeName = runtimeTypeName  ;
			this.runtimeType     = runtimeType      ;
			this.entries         = EqHashTable.New();
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String typeName()
		{
			return this.runtimeTypeName;
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition> entries()
		{
			return this.entries;
		}

		@Override
		public final Class<?> type()
		{
			return this.runtimeType;
		}

		@Override
		public final PersistenceTypeDefinition runtimeDefinition()
		{
			return this.runtimeDefinition;
		}
		
		@Override
		public final PersistenceTypeDefinition latest()
		{
			synchronized(this.entries)
			{
				return this.entries.values().peek();
			}
		}
		
		private void validate(final PersistenceTypeDefinition typeDefinition)
		{
			if(this.isValid(typeDefinition))
			{
				return;
			}
			
			// (12.10.2017 TM)EXCP: proper exception
			throw new RuntimeException("Invalid type definition for type lineage " + this.typeName());
		}
		
		private boolean isValid(final PersistenceTypeDefinition typeDefinition)
		{
			// checking runtimeTypeName is more precise than checking the type, as the prior might not be resolvable.
			if(!XChars.isEqual(this.runtimeTypeName, typeDefinition.runtimeTypeName()))
			{
				return false;
			}
			
			final PersistenceTypeDefinition alreadyRegistered = this.entries.get(typeDefinition.typeId());
			if(alreadyRegistered == null)
			{
				return true;
			}
			
			return PersistenceTypeDescriptionMember.equalDescriptions(
				typeDefinition.members(),
				alreadyRegistered.members()
			);
		}
		
		@Override
		public boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			this.validate(typeDefinition);
			return this.internalRegisterTypeDefinition(typeDefinition);
		}
		
		private boolean internalRegisterTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			synchronized(this.entries)
			{
				// the passed (and already validated) instance is always registered, ...
				if(this.entries.put(typeDefinition.typeId(), typeDefinition))
				{
					// ... but the return value is only true to indicate an actual additional entry.
					this.entries.keys().sort(Long::compare);
					return true;
				}
				
				// the definition was already there (and in order), only the instance has been replaced.
				return false;
			}
		}
				
		@Override
		public final boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition runtimeDefinition)
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
		
		private boolean checkViability(final PersistenceTypeDefinition runtimeDefinition)
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
