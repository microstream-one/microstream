package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDictionary extends SwizzleTypeDictionary
{
	public XGettingTable<Long, PersistenceTypeDefinition<?>> allTypes();
	
	public XGettingTable<String, PersistenceTypeDefinition<?>> latestTypesByName();
	
	public XGettingTable<Long, PersistenceTypeDefinition<?>> latestTypesById();
	
	public XGettingTable<String, PersistenceTypeLineage<?>> typeLineages();

	public boolean registerDefinitionEntry(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerDefinitionEntries(Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions);
	
	public boolean registerRuntimeDefinition(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerRuntimeDefinitions(Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeById(long typeId);

	public long determineHighestTypeId();

	public void setRegistrationCallback(PersistenceTypeDefinitionRegistrationCallback callback);

	public PersistenceTypeDefinitionRegistrationCallback getRegistrationCallback();
	
	public <T> PersistenceTypeLineage<T> ensureTypeLineage(String typeName, Class<T> type);
	
	public <T> PersistenceTypeLineage<T> lookupTypeLineage(String typeName);
	
	public <C extends Consumer<? super PersistenceTypeDefinition<?>>> C iterateAllTypes(C logic);
	
	


	public static PersistenceTypeDictionary New(final PersistenceTypeLineageCreator typeLineageCreator)
	{
		return new PersistenceTypeDictionary.Implementation(
			notNull(typeLineageCreator)
		);
	}

	public final class Implementation implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// the dictionary must be enhanceable at runtime, hence it must know a type lineage provider
		private final PersistenceTypeLineageCreator                     typeLineageCreator  ;
		private final EqHashTable<String, PersistenceTypeLineage<?>>    typeLineages         = EqHashTable.New();
		
		private final EqHashTable<Long  , PersistenceTypeDefinition<?>> allTypesPerTypeId    = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDefinition<?>> latestTypesPerName   = EqHashTable.New();
		private final EqHashTable<Long, PersistenceTypeDefinition<?>>   latestTypesPerTypeId = EqHashTable.New();
		
		private       PersistenceTypeDefinitionRegistrationCallback     registrationCallback;
		


		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final PersistenceTypeLineageCreator typeLineageCreator)
		{
			super();
			this.typeLineageCreator = typeLineageCreator;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public synchronized <C extends Consumer<? super PersistenceTypeDefinition<?>>> C iterateAllTypes(final C logic)
		{
			// iterate under lock protection to guarantee consistency
			return this.allTypesPerTypeId.values().iterate(logic);
		}
		
		@Override
		public synchronized <T> PersistenceTypeLineage<T> lookupTypeLineage(final String typeName)
		{
			// it is the caller's responsibility to pass the proper typeName representing the specified type T.
			@SuppressWarnings("unchecked")
			final PersistenceTypeLineage<T> lineage =
				(PersistenceTypeLineage<T>)this.typeLineages.get(typeName)
			;
			return lineage;
		}

		@Override
		public synchronized <T> PersistenceTypeLineage<T> ensureTypeLineage(final String typeName, final Class<T> type)
		{
			PersistenceTypeLineage<T> lineage = this.lookupTypeLineage(typeName);
			if(lineage != null)
			{
				return lineage;
			}
			
			lineage = this.typeLineageCreator.createTypeLineage(typeName, type);
			this.typeLineages.add(typeName, lineage);

			return lineage;
		}
					
		@Override
		public synchronized void setRegistrationCallback(
			final PersistenceTypeDefinitionRegistrationCallback callback
		)
		{
			this.registrationCallback = callback;
		}

		@Override
		public final synchronized PersistenceTypeDefinitionRegistrationCallback getRegistrationCallback()
		{
			return this.registrationCallback;
		}
		
		@Override
		public final synchronized XGettingTable<String, PersistenceTypeLineage<?>> typeLineages()
		{
			return this.typeLineages;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition<?>> allTypes()
		{
			return this.allTypesPerTypeId;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition<?>> latestTypesById()
		{
			return this.latestTypesPerTypeId;
		}
		
		@Override
		public final synchronized XGettingTable<String, PersistenceTypeDefinition<?>> latestTypesByName()
		{
			return this.latestTypesPerName;
		}

		@Override
		public final synchronized boolean registerDefinitionEntry(final PersistenceTypeDefinition<?> typeDefinition)
		{
			final boolean result = this.internalAddTypeEntry(typeDefinition);
			this.updateLatestTypeDefinitions();
			return result;
		}

		@Override
		public synchronized boolean registerDefinitionEntries(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			final long oldSize = this.allTypesPerTypeId.size();

			for(final PersistenceTypeDefinition<?> td : typeDefinitions)
			{
				this.internalAddTypeEntry(td);
			}
			this.updateLatestTypeDefinitions();

			return this.allTypesPerTypeId.size() != oldSize;
		}
		
		final <T> boolean internalRegisterTypeEntry(final PersistenceTypeDefinition<T> typeDefinition)
		{
			final PersistenceTypeLineage<T> lineage = this.ensureTypeLineage(
				typeDefinition.typeName(),
				typeDefinition.type()
			);
			
			lineage.registerTypeDefinition(typeDefinition);
			
			// callback gets set externally, can be null as well, so it must be checked.
			if(this.registrationCallback != null)
			{
				this.registrationCallback.registerTypeDefinition(typeDefinition);
			}
			
			return true;
		}
		
		private boolean validatingAddByTypeId(final PersistenceTypeDefinition<?> typeDefinition)
		{
			final KeyValue<Long, PersistenceTypeDefinition<?>> alreadyRegistered = this.allTypesPerTypeId.addGet(
				typeDefinition.typeId(),
				typeDefinition
			);
			
			if(alreadyRegistered != null)
			{
				if(alreadyRegistered.value() == typeDefinition)
				{
					// already registered, abort.
					return false;
				}
				// (13.10.2017 TM)EXCP: proper exception
				throw new RuntimeException("TypeId already registered: " + typeDefinition.typeId());
			}
			
			return true;
		}
		
		final <T> boolean internalAddTypeEntry(final PersistenceTypeDefinition<T> typeDefinition)
		{
			if(!this.validatingAddByTypeId(typeDefinition))
			{
				return false;
			}
			
			final PersistenceTypeLineage<T> lineage = this.ensureTypeLineage(
				typeDefinition.typeName(),
				typeDefinition.type()
			);
			
			if(!lineage.registerTypeDefinition(typeDefinition))
			{
				// (13.10.2017 TM)EXCP: proper exception
				throw new RuntimeException("Inconsistent type lineage for TypeId " + typeDefinition.typeId());
			}
			
			return true;
		}
		
		@Override
		public boolean registerRuntimeDefinition(final PersistenceTypeDefinition<?> typeDefinition)
		{
			final boolean result = this.internalPutRuntimeType(typeDefinition);
			this.updateLatestTypeDefinitions();
			return result;
		}
		
		@Override
		public boolean registerRuntimeDefinitions(final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions)
		{
			final long oldSize = this.allTypesPerTypeId.size();

			for(final PersistenceTypeDefinition<?> td : typeDefinitions)
			{
				this.internalPutRuntimeType(td);
			}
			this.updateLatestTypeDefinitions();

			return this.allTypesPerTypeId.size() != oldSize;
		}
		
		final <T> boolean internalPutRuntimeType(final PersistenceTypeDefinition<T> typeDefinition)
		{
			final PersistenceTypeLineage<T> lineage = this.ensureTypeLineage(
				typeDefinition.typeName(),
				typeDefinition.type()
			);
						
			if(!lineage.initializeRuntimeTypeDefinition(typeDefinition))
			{
				// type definition already contained, abort.
				return false;
			}

			// the runtime type to be registered always replaces a potentially preexisting type
			this.allTypesPerTypeId.put(typeDefinition.typeId(), typeDefinition);
			
			// callback gets set externally, can be null as well, so it must be checked.
			if(this.registrationCallback != null)
			{
				this.registrationCallback.registerTypeDefinition(typeDefinition);
			}
			
			return true;
		}
		
		private void updateLatestTypeDefinitions()
		{
			this.latestTypesPerTypeId.clear();
			this.latestTypesPerName  .clear();
			
			for(final PersistenceTypeLineage<?> lineage : this.typeLineages.values())
			{
				final PersistenceTypeDefinition<?> latest = lineage.latest();
				this.latestTypesPerTypeId.put(latest.typeId()  , latest);
				this.latestTypesPerName  .put(latest.typeName(), latest);
			}
		
			// sort allTypesPerTypeId here as well, because it got changed before in any case.
			this.allTypesPerTypeId   .keys().sort(JadothSort::compare);
			this.latestTypesPerName  .keys().sort(JadothSort::compare);
			this.latestTypesPerTypeId.keys().sort(JadothSort::compare);
		}
		
		@Override
		public synchronized PersistenceTypeDefinition<?> lookupTypeByName(final String typeName)
		{
			return this.latestTypesPerName.get(typeName);
		}

		@Override
		public synchronized PersistenceTypeDefinition<?> lookupTypeById(final long typeId)
		{
			return this.allTypesPerTypeId.get(typeId);
		}

		@Override
		public synchronized long determineHighestTypeId()
		{
			long maxTypeId = -1;
			
			for(final PersistenceTypeDefinition<?> type : this.allTypesPerTypeId.values())
			{
				if(type.typeId() >= maxTypeId)
				{
					maxTypeId = type.typeId();
				}
			}

			return maxTypeId;
		}

		@Override
		public synchronized String toString()
		{
			final VarString vc = VarString.New();
			
			JadothSort.valueSort(this.allTypesPerTypeId.values(), SwizzleTypeIdOwner::orderAscending);
			for(final PersistenceTypeDefinition<?> type : this.allTypesPerTypeId.values())
			{
				vc.add(type).lf();
			}

			return vc.toString();
		}
		
	}
	
	public static boolean isVariableLength(final String typeName)
	{
		switch(typeName)
		{
			case Symbols.TYPE_BYTES:
			case Symbols.TYPE_CHARS:
			case Symbols.TYPE_COMPLEX:
			{
					return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static String fullQualifiedFieldName(
		final String declaringTypeName,
		final String fieldName
	)
	{
		return fullQualifiedFieldName(VarString.New(), declaringTypeName, fieldName).toString();
	}

	public static VarString fullQualifiedFieldName(
		final VarString vc               ,
		final String    declaringTypeName,
		final String    fieldName
	)
	{
		return vc.add(declaringTypeName).add(Symbols.MEMBER_FIELD_DECL_TYPE_SEPERATOR).add(fieldName);
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the apropriate member types
	public static boolean isInlinedComplexType(final String typeName)
	{
		return Symbols.TYPE_COMPLEX.equals(typeName);
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the apropriate member types
	public static boolean isInlinedVariableLengthType(final String typeName)
	{
		return Symbols.TYPE_BYTES.equals(typeName)
			|| Symbols.TYPE_CHARS.equals(typeName)
			|| isInlinedComplexType(typeName)
		;
	}



	public class Symbols
	{
		protected static final transient char   TYPE_START                       = '{';
		protected static final transient char   TYPE_END                         = '}';
		protected static final transient char   MEMBER_FIELD_DECL_TYPE_SEPERATOR = '#';
		protected static final transient char   MEMBER_TERMINATOR                = ','; // cannot be ";" as array names are terminated by it
		protected static final transient char   MEMBER_COMPLEX_DEF_START         = '(';
		protected static final transient char   MEMBER_COMPLEX_DEF_END           = ')';

		protected static final transient String KEYWORD_PRIMITIVE                = "primitive";
		protected static final transient String TYPE_CHARS                       = "[char]"   ;
		protected static final transient String TYPE_BYTES                       = "[byte]"   ;
		protected static final transient String TYPE_COMPLEX                     = "[list]"   ;
		protected static final transient int    LITERAL_LENGTH_TYPE_COMPLEX      = TYPE_COMPLEX.length();

		protected static final transient char[] ARRAY_KEYWORD_PRIMITIVE          = KEYWORD_PRIMITIVE.toCharArray();
		protected static final transient char[] ARRAY_TYPE_CHARS                 = TYPE_CHARS       .toCharArray();
		protected static final transient char[] ARRAY_TYPE_BYTES                 = TYPE_BYTES       .toCharArray();
		protected static final transient char[] ARRAY_TYPE_COMPLEX               = TYPE_COMPLEX     .toCharArray();

		public static final String typeChars()
		{
			return TYPE_CHARS;
		}

		public static final String typeBytes()
		{
			return TYPE_BYTES;
		}

		public static final String typeComplex()
		{
			return TYPE_COMPLEX;
		}



		protected Symbols()
		{
			super();
			// can be extended to access the symbols
		}

	}

	
}
