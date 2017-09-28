package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDictionary extends SwizzleTypeDictionary
{
	public XGettingTable<Long, PersistenceTypeDefinition<?>> allTypes();
	
	public XGettingTable<String, PersistenceTypeDefinition<?>> latestTypesByName();
	
	public XGettingTable<Long, PersistenceTypeDefinition<?>> latestTypesPerTId();
	
	public XGettingTable<String, PersistenceTypeLineage<?>> types();

	public boolean registerType(PersistenceTypeDefinition<?> typeDescription);

	public boolean registerTypes(Iterable<? extends PersistenceTypeDefinition<?>> typeDescriptions);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeById(long typeId);

	public long determineHighestTypeId();

	public void setTypeDescriptionRegistrationCallback(PersistenceTypeDescriptionRegistrationCallback callback);

	public PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback();
	
	public <T> PersistenceTypeLineage<T> ensureTypeLineage(String typeName);
	
	public <T> PersistenceTypeLineage<T> lookupTypeLineage(String typeName);
	
	public <C extends Consumer<? super PersistenceTypeDefinition<?>>> C iterateAllTypes(C logic);
	
	


	public static PersistenceTypeDictionary New(
		final PersistenceTypeLineageBuilder                 typeLineageBuilder,
		final XGettingCollection<PersistenceTypeLineage<?>> initialTypeLineages
	)
	{
		return new PersistenceTypeDictionary.Implementation(
			notNull(typeLineageBuilder),
			notNull(initialTypeLineages)
		);
	}

	public final class Implementation implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// the dictionary must be enhanceable at runtime, hence it must know a type lineage provider
		private final PersistenceTypeLineageBuilder                     typeLineageBuilder  ;
		private final EqHashTable<String, PersistenceTypeLineage<?>>    typeLineages         = EqHashTable.New();
		
		private final EqHashTable<Long  , PersistenceTypeDefinition<?>> allTypesPerTypeId    = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDefinition<?>> latestTypesPerName   = EqHashTable.New();
		private final EqHashTable<Long, PersistenceTypeDefinition<?>>   latestTypesPerTypeId = EqHashTable.New();
		
		private       PersistenceTypeDescriptionRegistrationCallback    registrationCallback;
		


		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeLineageBuilder                 typeLineageBuilder,
			final XGettingCollection<PersistenceTypeLineage<?>> initialTypeLineages
		)
		{
			super();
			this.typeLineageBuilder = typeLineageBuilder;
			
			for(final PersistenceTypeLineage<?> typeLineage : initialTypeLineages)
			{
				this.typeLineages.add(typeLineage.typeName(), typeLineage);
			}
			this.initializeLookupTables();
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private void initializeLookupTables()
		{
			for(final PersistenceTypeLineage<?> lineage : this.typeLineages.values())
			{
				for(final PersistenceTypeDefinition<?> td : lineage.entries().values())
				{
					this.putTypeDefinition(td);
				}
				this.putLatestTypeDefinition(lineage.latest());
			}
		}
		
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
		public synchronized <T> PersistenceTypeLineage<T> ensureTypeLineage(final String typeName)
		{
			PersistenceTypeLineage<T> lineage = this.lookupTypeLineage(typeName);
			if(lineage != null)
			{
				return lineage;
			}
			
			lineage = this.typeLineageBuilder.buildTypeLineage(typeName);
			this.typeLineages.add(typeName, lineage);

			return lineage;
		}
		
		final <T> boolean internalRegisterType(final PersistenceTypeDefinition<T> typeDefinition)
		{
			final PersistenceTypeLineage<T> lineage = this.ensureTypeLineage(typeDefinition.typeName());
						
			if(!lineage.initializeRuntimeTypeDefinition(typeDefinition))
			{
				// type definition already contained, abort.
				return false;
			}

			this.putLatestTypeDefinition(typeDefinition);
			
			// callback gets set externally, can be null as well, so it must be checked.
			if(this.registrationCallback != null)
			{
				this.registrationCallback.registerTypeDefinition(typeDefinition);
			}
			
			return true;
		}
		
		private void putTypeDefinition(final PersistenceTypeDefinition<?> typeDefinition)
		{
			// must be put, because a TypeHandler instance might replace a simple type definition.
			this.allTypesPerTypeId.put(typeDefinition.typeId(), typeDefinition);
		}
		
		private void putLatestTypeDefinition(final PersistenceTypeDefinition<?> typeDefinition)
		{
			// must be put, because a TypeHandler instance might replace a simple type definition.
			this.putTypeDefinition(typeDefinition);
			this.latestTypesPerTypeId.put(typeDefinition.typeId(), typeDefinition);
			this.latestTypesPerName.put(typeDefinition.typeName(), typeDefinition);
		}
		
		@Override
		public synchronized void setTypeDescriptionRegistrationCallback(
			final PersistenceTypeDescriptionRegistrationCallback callback
		)
		{
			this.registrationCallback = callback;
		}

		@Override
		public final synchronized PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback()
		{
			return this.registrationCallback;
		}
		
		@Override
		public final synchronized XGettingTable<String, PersistenceTypeLineage<?>> types()
		{
			return this.typeLineages;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition<?>> allTypes()
		{
			return this.allTypesPerTypeId;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition<?>> latestTypesPerTId()
		{
			return this.latestTypesPerTypeId;
		}
		
		@Override
		public final synchronized XGettingTable<String, PersistenceTypeDefinition<?>> latestTypesByName()
		{
			return this.latestTypesPerName;
		}

		@Override
		public final synchronized boolean registerType(final PersistenceTypeDefinition<?> typeDescription)
		{
			return this.internalRegisterType(typeDescription);
		}

		@Override
		public synchronized boolean registerTypes(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDescriptions
		)
		{
			final long oldSize = this.allTypesPerTypeId.size();

			for(final PersistenceTypeDefinition<?> td : typeDescriptions)
			{
				this.internalRegisterType(td);
			}

			return this.allTypesPerTypeId.size() != oldSize;
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
