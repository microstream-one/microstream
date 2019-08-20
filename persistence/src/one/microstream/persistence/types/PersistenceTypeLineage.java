package one.microstream.persistence.types;

import static one.microstream.X.mayNull;

import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;


public interface PersistenceTypeLineage
{
	public String typeName();
	
	public Class<?> type();
	
	public XGettingTable<Long, PersistenceTypeDefinition> entries();
	
	public PersistenceTypeDefinition latest();
	
	public PersistenceTypeDefinition runtimeDefinition();
	
	public PersistenceTypeLineageView view();
	
	

	// mutating logic //
	
	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);

	public boolean setRuntimeTypeDefinition(PersistenceTypeDefinition runtimeDefinition);
	
	
	
	public static PersistenceTypeLineage.Default New(
		final String   runtimeTypeName,
		final Class<?> runtimeType
	)
	{
		return new PersistenceTypeLineage.Default(
			mayNull(runtimeTypeName), // can be null for types explicitly mapped as having no runtime type.
			mayNull(runtimeType)      // can be null if the type name cannot be resolved to a runtime class.
		);
	}
		
	public final class Default implements PersistenceTypeLineage
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

		Default(final String runtimeTypeName, final Class<?> runtimeType)
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
		public final Class<?> type()
		{
			return this.runtimeType;
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition> entries()
		{
			return this.entries;
		}

		@Override
		public final synchronized PersistenceTypeDefinition runtimeDefinition()
		{
			return this.runtimeDefinition;
		}
		
		@Override
		public final synchronized PersistenceTypeDefinition latest()
		{
			return this.entries.values().peek();
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
			
			return PersistenceTypeDescriptionMember.equalStructures(
				typeDefinition.allMembers(),
				alreadyRegistered.allMembers()
			);
		}
		
		@Override
		public synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			this.validate(typeDefinition);
			return this.synchRegisterTypeDefinition(typeDefinition);
		}
		
		private boolean synchRegisterTypeDefinition(final PersistenceTypeDefinition typeDefinition)
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
				
		@Override
		public final synchronized boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition runtimeDefinition)
		{
			// false indicates no-op, actual non-viability causes exceptions
			if(!this.synchCheckViability(runtimeDefinition))
			{
				return false;
			}
			
			// normal case: effective final initialization
			this.runtimeDefinition = runtimeDefinition;
			
			// correct behavior of the put has been checked above
			this.entries.put(runtimeDefinition.typeId(), runtimeDefinition);
			
			return true;
		}
		
		private boolean synchCheckViability(final PersistenceTypeDefinition runtimeDefinition)
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
		
		@Override
		public synchronized PersistenceTypeLineageView view()
		{
			return PersistenceTypeLineageView.New(this);
		}
		
		@Override
		public String toString()
		{
			return PersistenceTypeLineage.class.getSimpleName() + " " + this.runtimeTypeName + " " + this.entries.keys();
		}
		
	}

}
