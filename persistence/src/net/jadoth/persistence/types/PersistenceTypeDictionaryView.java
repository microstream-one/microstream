package net.jadoth.persistence.types;

import static net.jadoth.persistence.types.PersistenceTypeDictionary.assembleTypesPerTypeId;

import net.jadoth.chars.VarString;
import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingTable;


/**
 * A read-only ("view") type of a {@link PersistenceTypeDictionary} where all mutating methods throw an
 * {@link UnsupportedOperationException}.<p>
 * Conceptual note:<br>
 * The natural concept would be to design the reading type as the base type with an immutable implementation
 * and extend a mutating type from that base type. The intial concept was exactely that. But it turned out that the
 * the way type dictionaries are used contradicts that and could better incorporate the described reversed hierarchy.
 * <br>
 * That way is:<br>
 * Mutating operations are encaspulated by ~Manager types and are rejected with an exception for immutable usage.
 * Reading operations work seamless in both concepts, anyway.
 * 
 * @author TM
 */
public interface PersistenceTypeDictionaryView extends PersistenceTypeDictionary
{
	@Override
	public XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions();
	
	@Override
	public XGettingTable<String, ? extends PersistenceTypeLineageView> typeLineages();
			
	@Override
	public PersistenceTypeLineageView lookupTypeLineage(Class<?> type);
	
	@Override
	public PersistenceTypeLineageView lookupTypeLineage(String typeName);
	
	@Override
	public default PersistenceTypeDictionaryView view()
	{
		return this;
	}
	
	@Override
	public default PersistenceTypeLineage ensureTypeLineage(final Class<?> type)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public default boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean registerTypeDefinitions(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean registerRuntimeTypeDefinition(final PersistenceTypeDefinition typeDefinition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean registerRuntimeTypeDefinitions(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default void setTypeDescriptionRegistrationObserver(final PersistenceTypeDefinitionRegistrationObserver observer)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver()
	{
		throw new UnsupportedOperationException();
	}
	
	

	public static PersistenceTypeDictionaryView New(final PersistenceTypeDictionary typeDictionary)
	{
		synchronized(typeDictionary)
		{
			return new PersistenceTypeDictionaryView.Implementation(
				EqConstHashTable.New(typeDictionary.typeLineages(), PersistenceTypeLineageView::New),
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
		private final EqConstHashTable<Long  , PersistenceTypeDefinition>            allTypesPerTypeId;



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
		public final PersistenceTypeDictionaryView view()
		{
			// this implementation is already an immutable view.
			return this;
		}

		@Override
		public final String toString()
		{
			return assembleTypesPerTypeId(VarString.New(), this.allTypesPerTypeId).toString();
		}

	}

}
