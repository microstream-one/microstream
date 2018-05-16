package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.chars.VarString;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;

public interface PersistenceTypeDictionary extends SwizzleTypeDictionary
{
	public XGettingTable<Long, PersistenceTypeDefinition<?>> allTypeDefinitions();
	
	public XGettingTable<String, PersistenceTypeLineage<?>> typeLineages();
	
	public boolean isEmpty();

	public boolean registerTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerTypeDefinitions(Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions);

	public boolean registerRuntimeTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerRuntimeTypeDefinitions(Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeById(long typeId);

	public long determineHighestTypeId();

	public void setTypeDescriptionRegistrationObserver(PersistenceTypeDefinitionRegistrationObserver observer);

	public PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver();
	
	public <T> PersistenceTypeLineage<T> ensureTypeLineage(Class<T> type);
	
	public PersistenceTypeLineage<?> ensureTypeLineage(String typeName);
	
	public <T> PersistenceTypeLineage<T> lookupTypeLineage(Class<T> type);
	
	public PersistenceTypeLineage<?> lookupTypeLineage(String typeName);
	
	public default <C extends Consumer<? super PersistenceTypeDefinition<?>>> C iterateAllTypeDefinitions(final C logic)
	{
		return this.allTypeDefinitions().values().iterate(logic);
	}
	
	public default <C extends Consumer<? super PersistenceTypeDefinition<?>>> C iterateRuntimeDefinitions(final C logic)
	{
		this.iterateTypeLineages(tl ->
		{
			logic.accept(tl.runtimeDefinition());
		});
		
		return logic;
	}
	
	public default <C extends Consumer<? super PersistenceTypeDefinition<?>>> C iterateLatestTypes(final C logic)
	{
		this.iterateTypeLineages(tl ->
		{
			logic.accept(tl.latest());
		});
		
		return logic;
	}
	
	public default <C extends Consumer<? super PersistenceTypeLineage<?>>> C iterateTypeLineages(final C logic)
	{
		return this.typeLineages().values().iterate(logic);
	}

	

	public static <D extends PersistenceTypeDictionary> D registerTypes(
		final D                                                          typeDictionary  ,
		final XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDefinitions
	)
	{
		typeDictionary.registerTypeDefinitions(typeDefinitions);
		return typeDictionary;
	}

	public static PersistenceTypeDictionary New(final PersistenceTypeLineageCreator typeLineageCreator)
	{
		return new PersistenceTypeDictionary.Implementation(
			notNull(typeLineageCreator)
		);
	}

	public static PersistenceTypeDictionary New(
		final PersistenceTypeLineageCreator                              typeLineageCreator,
		final XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDefinitions
	)
	{
		return PersistenceTypeDictionary.registerTypes(
			New(typeLineageCreator),
			typeDefinitions
		);
	}

	public final class Implementation implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// (05.04.2017 TM)FIXME: OGS-3: distinct between all types and live types

		// the dictionary must be enhanceable at runtime, hence it must know a type lineage provider
		private final PersistenceTypeLineageCreator                     typeLineageCreator;
		private final EqHashTable<String, PersistenceTypeLineage<?>>    typeLineages       = EqHashTable.New();
		
		private final EqHashTable<Long  , PersistenceTypeDefinition<?>> allTypesPerTypeId  = EqHashTable.New();
		private       PersistenceTypeDefinitionRegistrationObserver     registrationObserver;



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
		public final XGettingTable<String, PersistenceTypeLineage<?>> typeLineages()
		{
			return this.typeLineages;
		}
		
		@Override
		public synchronized <T> PersistenceTypeLineage<T> lookupTypeLineage(final Class<T> type)
		{
			return this.synchLookupTypeLineage(type.getName());
		}
		
		@Override
		public synchronized PersistenceTypeLineage<?> lookupTypeLineage(final String typeName)
		{
			return this.synchLookupTypeLineage(typeName);
		}
		
		private <T> PersistenceTypeLineage<T> synchLookupTypeLineage(final String typeName)
		{
			// The type safety of this cast is ensured by the calling contexts' logic.
			@SuppressWarnings("unchecked")
			final PersistenceTypeLineage<T> lineage =
				(PersistenceTypeLineage<T>)this.typeLineages.get(typeName)
			;
			return lineage;
		}
		
		@Override
		public synchronized <T> PersistenceTypeLineage<T> ensureTypeLineage(final Class<T> type)
		{
			final PersistenceTypeLineage<T> lineage = this.lookupTypeLineage(type);
			if(lineage != null)
			{
				return lineage;
			}
			
			return synchRegisterTypeLineage(this.typeLineageCreator.createTypeLineage(type));
		}
		
		private <T> PersistenceTypeLineage<T> synchRegisterTypeLineage(final PersistenceTypeLineage<T> lineage)
		{
			this.typeLineages.add(lineage.typeName(), lineage);
			this.synchSortTypeLineages();

			return lineage;
		}
		
		@Override
		public synchronized PersistenceTypeLineage<?> ensureTypeLineage(final String typeName)
		{
			final PersistenceTypeLineage<?> lineage = this.lookupTypeLineage(typeName);
			if(lineage != null)
			{
				return lineage;
			}

			return synchRegisterTypeLineage(this.typeLineageCreator.createTypeLineage(typeName));
		}

		final <T> boolean synchRegisterType(final PersistenceTypeDefinition<T> typeDefinition)
		{
			final PersistenceTypeLineage<T> lineage = this.ensureTypeLineage(typeDefinition.type());
						
			if(!lineage.registerTypeDefinition(typeDefinition))
			{
				// type definition already contained, abort.
				return false;
			}
			
			// definitions can be replaced by another instance (e.g. a plain instance by a handler instance)
			this.allTypesPerTypeId.put(typeDefinition.typeId(), typeDefinition);

			// callback gets set externally, can be null as well, so check for it.
			if(this.registrationObserver != null)
			{
				this.registrationObserver.observeTypeDefinitionRegistration(typeDefinition);
			}
			
			return true;
		}
		
		private void synchSortTypeLineages()
		{
			this.typeLineages.keys().sort(XSort::compare);
		}

		private void internalSort()
		{
			this.allTypesPerTypeId.keys().sort(XSort::compare);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized void setTypeDescriptionRegistrationObserver(
			final PersistenceTypeDefinitionRegistrationObserver registrationObserver
		)
		{
			this.registrationObserver = registrationObserver;
		}

		@Override
		public final synchronized PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver()
		{
			return this.registrationObserver;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition<?>> allTypeDefinitions()
		{
			return this.allTypesPerTypeId;
		}
		
		@Override
		public final synchronized boolean isEmpty()
		{
			return this.allTypesPerTypeId.isEmpty();
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition<?> typeDefinition)
		{
			if(this.synchRegisterType(typeDefinition))
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			final long oldSize = this.allTypesPerTypeId.size();

			for(final PersistenceTypeDefinition<?> td : typeDefinitions)
			{
				this.synchRegisterType(td);
			}

			if(this.allTypesPerTypeId.size() != oldSize)
			{
				this.internalSort();
				return true;
			}
			
			return false;
		}
		
		@Override
		public final synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition<?> typeDefinition
		)
		{
			final boolean returnValue = this.registerTypeDefinition(typeDefinition);
			this.synchSetRuntimeTypeDefinition(typeDefinition);
			
			return returnValue;
		}
		
		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			final boolean returnValue = this.registerTypeDefinitions(typeDefinitions);
			
			for(final PersistenceTypeDefinition<?> td : typeDefinitions)
			{
				this.synchSetRuntimeTypeDefinition(td);
			}
			
			return returnValue;
		}
		
		private <T> void synchSetRuntimeTypeDefinition(final PersistenceTypeDefinition<T> td)
		{
			final PersistenceTypeLineage<T> lineage = this.lookupTypeLineage(td.type());
			lineage.setRuntimeTypeDefinition(td);
		}

		@Override
		public final synchronized PersistenceTypeDefinition<?> lookupTypeByName(final String typeName)
		{
			final PersistenceTypeLineage<?> lineage = this.lookupTypeLineage(typeName);
			
			return lineage == null
				? null
				: lineage.latest()
			;
		}

		@Override
		public final synchronized PersistenceTypeDefinition<?> lookupTypeById(final long typeId)
		{
			return this.allTypesPerTypeId.get(typeId);
		}

		@Override
		public final synchronized long determineHighestTypeId()
		{
			long maxTypeId = -1;

			for(final Long typeId : this.allTypesPerTypeId.keys())
			{
				if(typeId >= maxTypeId)
				{
					maxTypeId = typeId;
				}
			}

			return maxTypeId;
		}

		@Override
		public final synchronized String toString()
		{
			final VarString vc = VarString.New();

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

	public static VarString paddedFullQualifiedFieldName(
		final VarString vc                        ,
		final String  declaringTypeName         ,
		final int     maxDeclaringTypeNameLength,
		final String  fieldName                 ,
		final int     maxFieldNameLength
	)
	{
		// redundant code here to avoid unnecessary padding in normal case
		return vc
			.padRight(declaringTypeName, maxDeclaringTypeNameLength, ' ')
			.add(Symbols.MEMBER_FIELD_DECL_TYPE_SEPERATOR)
			.padRight(fieldName        , maxFieldNameLength        , ' ')
		;
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
