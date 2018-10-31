package net.jadoth.persistence.types;

import static net.jadoth.persistence.types.PersistenceTypeDictionary.assembleTypesPerTypeId;

import java.util.function.Consumer;

import net.jadoth.chars.VarString;
import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDictionary;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;


public interface PersistenceTypeDictionaryView extends SwizzleTypeDictionary
{
	public XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions();
	
	public XGettingTable<String, ? extends PersistenceTypeLineageView> typeLineages();
	
	public boolean isEmpty();

	@Override
	public PersistenceTypeDefinition lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDefinition lookupTypeById(long typeId);

	public long determineHighestTypeId();
		
	public PersistenceTypeLineageView lookupTypeLineage(Class<?> type);
	
	public PersistenceTypeLineageView lookupTypeLineage(String typeName);
	
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C iterateAllTypeDefinitions(final C logic)
	{
		return this.allTypeDefinitions().values().iterate(logic);
	}
	
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C iterateRuntimeDefinitions(final C logic)
	{
		this.iterateTypeLineageViews(tl ->
		{
			logic.accept(tl.runtimeDefinition());
		});
		
		return logic;
	}
	
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C resolveTypeIds(
		final Iterable<Long> typeIds  ,
		final C              collector
	)
	{
		for(final Long typeId : typeIds)
		{
			final PersistenceTypeDefinition typeDefinition = this.lookupTypeById(typeId);
			if(typeDefinition == null)
			{
				throw new PersistenceExceptionTypeConsistencyDictionary("TypeId cannot be resolved: " + typeId);
			}
			
			collector.accept(typeDefinition);
		}
		
		return collector;
	}
	
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C iterateLatestTypes(final C logic)
	{
		this.iterateTypeLineageViews(tl ->
		{
			logic.accept(tl.latest());
		});
		
		return logic;
	}
	
	public default <C extends Consumer<? super PersistenceTypeLineageView>> C iterateTypeLineageViews(final C logic)
	{
		return this.typeLineages().values().iterate(logic);
	}

	

	public static PersistenceTypeDictionaryView New(final PersistenceTypeDictionary typeDictionary)
	{
		synchronized(typeDictionary)
		{
			return new PersistenceTypeDictionaryView.Implementation(
				EqConstHashTable.New(typeDictionary.typeLineages()),
				EqConstHashTable.New(typeDictionary.allTypeDefinitions())
			);
		}
	}
	
	public final class Implementation implements PersistenceTypeDictionaryView
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final EqConstHashTable<String, ? extends PersistenceTypeLineageView> typeLineages     ;
		private final EqConstHashTable<Long  , PersistenceTypeDefinition> allTypesPerTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final EqConstHashTable<String, ? extends PersistenceTypeLineageView> typeLineages     ,
			final EqConstHashTable<Long  , PersistenceTypeDefinition>            allTypesPerTypeId
		)
		{
			super();
			this.typeLineages      = typeLineages     ;
			this.allTypesPerTypeId = allTypesPerTypeId;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final XGettingTable<String, ? extends PersistenceTypeLineageView> typeLineages()
		{
			return this.typeLineages;
		}
		
		@Override
		public synchronized PersistenceTypeLineageView lookupTypeLineage(final Class<?> type)
		{
			return this.synchLookupTypeLineage(type.getName());
		}
		
		@Override
		public synchronized PersistenceTypeLineageView lookupTypeLineage(final String typeName)
		{
			return this.synchLookupTypeLineage(typeName);
		}
		
		private <T> PersistenceTypeLineageView synchLookupTypeLineage(final String typeName)
		{
			final PersistenceTypeLineageView lineage = this.typeLineages.get(typeName);
			return lineage;
		}
		
		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions()
		{
			return this.allTypesPerTypeId;
		}
		
		@Override
		public final boolean isEmpty()
		{
			return this.allTypesPerTypeId.isEmpty();
		}
		
		@Override
		public final PersistenceTypeDefinition lookupTypeByName(final String typeName)
		{
			final PersistenceTypeLineageView lineage = this.lookupTypeLineage(typeName);
			
			return lineage == null
				? null
				: lineage.latest()
			;
		}

		@Override
		public final PersistenceTypeDefinition lookupTypeById(final long typeId)
		{
			return this.allTypesPerTypeId.get(typeId);
		}

		@Override
		public final long determineHighestTypeId()
		{
			return PersistenceTypeDictionary.determineHighestTypeId(this.allTypesPerTypeId);
		}

		@Override
		public final String toString()
		{
			return assembleTypesPerTypeId(VarString.New(), this.allTypesPerTypeId).toString();
		}

	}

}
