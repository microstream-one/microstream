package net.jadoth.persistence.types;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingTable;


public interface PersistenceTypeLineageView
{
	public String typeName();
	
	public Class<?> type();
	
	public XGettingTable<Long, PersistenceTypeDefinition> entries();
	
	public PersistenceTypeDefinition latest();
	
	public PersistenceTypeDefinition runtimeDefinition();
	
	
	
	public static PersistenceTypeLineageView New(final PersistenceTypeLineage typeLineage)
	{
		synchronized(typeLineage)
		{
			return new PersistenceTypeLineageView.Implementation(
				typeLineage.typeName(),
				typeLineage.type()    ,
				EqConstHashTable.New(typeLineage.entries()),
				typeLineage.runtimeDefinition()
			);
		}
	}
		
	public final class Implementation implements PersistenceTypeLineageView
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                            runtimeTypeName  ;
		final Class<?>                                          runtimeType      ;
		final EqConstHashTable<Long, PersistenceTypeDefinition> entries          ;
		final PersistenceTypeDefinition                         runtimeDefinition;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String                                            runtimeTypeName  ,
			final Class<?>                                          runtimeType      ,
			final EqConstHashTable<Long, PersistenceTypeDefinition> entries          ,
			final PersistenceTypeDefinition                         runtimeDefinition
		)
		{
			super();
			this.runtimeTypeName   = runtimeTypeName  ;
			this.runtimeType       = runtimeType      ;
			this.entries           = entries          ;
			this.runtimeDefinition = runtimeDefinition;
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
			return this.entries.values().peek();
		}
				
	}

}
