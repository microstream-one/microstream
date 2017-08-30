package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingTable;

public interface PersistenceTypeDescriptionLineage<T>
{
	public String typeName();
	
	public XGettingTable<Long, PersistenceTypeDescription<T>> members();
	
	public default PersistenceTypeDescription<T> latest()
	{
		return this.members().values().peek();
	}
	
	public Class<T> runtimeType();
	
	public PersistenceTypeDescription<T> current();
	
	public boolean isValid();
	
	
	
	
	public static <T> PersistenceTypeDescriptionLineage<T> New(
		final String                                             typeName   ,
		final XGettingTable<Long, PersistenceTypeDescription<T>> members    ,
		final Class<T>                                           runtimeType,
		final PersistenceTypeDescription<T>                      current    ,
		final boolean                                            isValid
	)
	{
		return new PersistenceTypeDescriptionLineage.Implementation<>(
			notNull(typeName), // may never be null as this is the lineage's identity.
			notNull(members) , // may be initially empty, but never null.
			runtimeType      , // can be null if the type can not be resolved into a runtime class.
			current          , // can be null
			isValid
		);
	}
	
	
	public final class Implementation<T> implements PersistenceTypeDescriptionLineage<T>
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                             typeName   ;
		final Class<T>                                           runtimeType;
		final XGettingTable<Long, PersistenceTypeDescription<T>> members    ;
		      PersistenceTypeDescription<T>                      current    ; // effective final

		transient Boolean isValid;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String                                             typeName   ,
			final XGettingTable<Long, PersistenceTypeDescription<T>> members    ,
			final Class<T>                                           runtimeType,
			final PersistenceTypeDescription<T>                      current    ,
			final boolean                                            isValid
		)
		{
			super();
			this.typeName    = typeName   ;
			this.members     = members    ;
			this.runtimeType = runtimeType;
			this.current     = current    ;
			this.isValid     = isValid    ;
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
		public final XGettingTable<Long, PersistenceTypeDescription<T>> members()
		{
			return this.members;
		}

		@Override
		public final Class<T> runtimeType()
		{
			return this.runtimeType;
		}

		@Override
		public final PersistenceTypeDescription<T> current()
		{
			return this.current;
		}

		@Override
		public final synchronized boolean isValid()
		{
			if(this.isValid == null)
			{
				this.updateValidity();
			}
			
			return this.isValid;
		}
		
		final synchronized boolean updateValidity()
		{
			return this.isValid = PersistenceTypeDescription.isEqualDescription(this.latest(), this.current());
		}
		
		final synchronized void initializeCurrent(final PersistenceTypeDescription<T> current)
		{
			if(this.current == current)
			{
				// no-op
				return;
			}
			else if(this.current != null)
			{
				// (30.08.2017 TM)EXCP: proper exception
				throw new RuntimeException(
					"Current " + PersistenceTypeDescription.class.getSimpleName() + " already initialized."
				);
			}
			
			this.current = current;
		}

	}

}
