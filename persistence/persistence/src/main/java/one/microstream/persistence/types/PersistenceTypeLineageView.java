package one.microstream.persistence.types;

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.types.XGettingTable;


public interface PersistenceTypeLineageView extends PersistenceTypeLineage
{
	@Override
	public default PersistenceTypeLineageView view()
	{
		return this;
	}
	
	@Override
	public default boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition runtimeDefinition)
	{
		throw new UnsupportedOperationException();
	}
	
	
	public static PersistenceTypeLineageView New(final PersistenceTypeLineage typeLineage)
	{
		synchronized(typeLineage)
		{
			return new PersistenceTypeLineageView.Default(
				typeLineage.typeName()                     ,
				typeLineage.type()                         ,
				EqConstHashTable.New(typeLineage.entries()),
				typeLineage.runtimeDefinition()
			);
		}
	}
		
	public final class Default implements PersistenceTypeLineageView
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

		Default(
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
