package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingTable;

public interface PersistenceTypeDescriptionLineage<T>
{
	public String typeName();
	
	public XGettingTable<Long, PersistenceTypeDescription<T>> dictionaryEntries();
	
	public default PersistenceTypeDescription<T> latest()
	{
		return this.dictionaryEntries().values().peek();
	}
	
	public Class<T> runtimeType();
	
	public PersistenceTypeDescription<T> runtimeDescription();
	
	public boolean isValid();
	
	public boolean register(PersistenceTypeDescription<T> typeDescription);
	
	
	
	
	public static <T> PersistenceTypeDescriptionLineage<T> New(
		final String                        typeName   ,
		final PersistenceTypeDescription<T> runtimeType
	)
	{
		return new PersistenceTypeDescriptionLineage.Implementation<>(
			notNull(typeName), // may never be null as this is the lineage's identity.
			runtimeType        // can be null if the type can not be resolved into a runtime class.
		);
	}
	
	
	public final class Implementation<T> implements PersistenceTypeDescriptionLineage<T>
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                           typeName          ;
		final Class<T>                                         runtimeType       ;
		final EqHashTable<Long, PersistenceTypeDescription<T>> dictionaryEntries ;
		      PersistenceTypeDescription<T>                    runtimeDescription; // initialized effectively final

		transient boolean isValid;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String                        typeName          ,
			final PersistenceTypeDescription<T> runtimeDescription
		)
		{
			super();
			this.typeName           = typeName   ;
			this.dictionaryEntries  = EqHashTable.New();
			this.runtimeDescription = runtimeDescription;
			this.runtimeType        = runtimeDescription != null
				? runtimeDescription.type()
				: null
			;
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
		public final XGettingTable<Long, PersistenceTypeDescription<T>> dictionaryEntries()
		{
			return this.dictionaryEntries;
		}

		@Override
		public final Class<T> runtimeType()
		{
			return this.runtimeType;
		}

		@Override
		public final PersistenceTypeDescription<T> runtimeDescription()
		{
			return this.runtimeDescription;
		}

		@Override
		public final boolean isValid()
		{
			synchronized(this.dictionaryEntries)
			{
				return this.isValid;
			}
		}
		
		@Override
		public final boolean register(final PersistenceTypeDescription<T> typeDescription)
		{
			if(this.runtimeType != typeDescription.type())
			{
				// (01.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("Runtime type mismatch");
			}
			if(!this.typeName.equals(typeDescription.typeName()))
			{
				// (01.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("typeName mismatch");
			}
			if(this.runtimeType != null && typeDescription.typeId() > this.runtimeDescription.typeId())
			{
				// (01.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("TypeId greater than that of the runtime description");
			}
			
			synchronized(this.dictionaryEntries)
			{
				/*
				 * if the passed typeDescription is the same as or equal to the runtime-derived Description,
				 * the latter is used from this point on to consistently include the instance in the lineage.
				 */
				final PersistenceTypeDescription<T> effective =
					PersistenceTypeDescription.isEqualDescription(this.runtimeDescription, typeDescription)
					? this.runtimeDescription
					: typeDescription
				;
				
				if(this.dictionaryEntries.add(effective.typeId(), effective))
				{
					// the newly registered instance must be initialized to this lineage.
					effective.initializeLineage(this);
					
					// this check becomes very simply via the "effective" instance consolidation above.
					this.isValid = effective == this.runtimeDescription;
					
					JadothSort.valueSort(this.dictionaryEntries.keys(), Long::compareTo);
					
					// reporting back newly registered
					return true;
				}
				
				final PersistenceTypeDescription<T> registered = this.dictionaryEntries.get(effective.typeId());
				if(registered == effective)
				{
					// basically just used for validation
					effective.initializeLineage(this);
					
					// reporting back already registered (and consistent)
					return false;
				}
				
				// (01.09.2017 TM)EXCP: proper exception
				throw new RuntimeException(
					PersistenceTypeDescription.class.getSimpleName()
					+ " already registered for TypeId " + registered.typeId() + "."
				);
			}
		}
		
	}

}
