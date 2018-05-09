package net.jadoth.persistence.types;

import net.jadoth.chars.VarString;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;

public interface PersistenceTypeDictionary extends SwizzleTypeDictionary
{
	public XGettingEnum<PersistenceTypeDefinition<?>> allTypes();
	
	public XGettingTable<String, PersistenceTypeDefinition<?>> liveTypes();

	public boolean registerDefinitionEntry(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerDefinitionEntries(XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDefinitions);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDefinition<?> lookupTypeById(long typeId);

	public long determineHighestTypeId();

	public void setTypeDescriptionRegistrationCallback(PersistenceTypeDefinitionRegistrationCallback callback);

	public PersistenceTypeDefinitionRegistrationCallback getTypeDescriptionRegistrationCallback();


	public static <D extends PersistenceTypeDictionary> D initializeRegisteredTypes(
		final D                                                        typeDictionary  ,
		final XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDescriptions
	)
	{
		typeDictionary.registerDefinitionEntries(typeDescriptions);
		return typeDictionary;
	}

	public static PersistenceTypeDictionary New()
	{
		return new PersistenceTypeDictionary.Implementation();
	}

	public static PersistenceTypeDictionary New(
		final XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDescriptions
	)
	{
		return PersistenceTypeDictionary.initializeRegisteredTypes(
			New(),
			typeDescriptions
		);
	}





	public static boolean isVariableLength(final String typeName)
	{
		switch(typeName)
		{
			case Symbols.TYPE_BYTES:
			case Symbols.TYPE_CHARS:
			case Symbols.TYPE_COMPLEX: return true;
			default: return false;
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
		final VarString vc,
		final String  declaringTypeName,
		final String  fieldName
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



	public final class Implementation implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// (05.04.2017 TM)FIXME: OGS-3: distinct between all types and live types
		
		private final EqHashEnum<PersistenceTypeDefinition<?>> types;

		private final EqHashTable<Long  , PersistenceTypeDefinition<?>> allTypesPerTypeId  = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDefinition<?>> latestTypesPerName = EqHashTable.New();
		private       PersistenceTypeDefinitionRegistrationCallback     callback        ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation()
		{
			super();
			this.types = EqHashEnum.New(PersistenceTypeDefinition.EQUAL_TYPE);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void internalRegisterType(final PersistenceTypeDefinition<?> typeDescription)
		{
			this.allTypesPerTypeId.put(typeDescription.typeId(), typeDescription);
			this.latestTypesPerName.put(typeDescription.typeName(), typeDescription);

			// callback gets set externally, can be null as well, so check for it.
			if(this.callback != null)
			{
				this.callback.registerTypeDefinition(typeDescription);
			}
		}

		// (06.12.2014)TODO: rename "2"
		final boolean internalRegisterType2(final PersistenceTypeDefinition<?> typeDescription)
		{
			if(!this.types.add(typeDescription))
			{
				return false;
			}
			this.internalRegisterType(typeDescription);
			return true;
		}

		private void internalSort()
		{
			SwizzleTypeIdOwner.sortByTypeIdAscending(this.types);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void setTypeDescriptionRegistrationCallback(final PersistenceTypeDefinitionRegistrationCallback callback)
		{
			this.callback = callback;
		}

		@Override
		public PersistenceTypeDefinitionRegistrationCallback getTypeDescriptionRegistrationCallback()
		{
			return this.callback;
		}

		@Override
		public XGettingEnum<PersistenceTypeDefinition<?>> allTypes()
		{
			return this.types;
		}
		
		@Override
		public XGettingTable<String, PersistenceTypeDefinition<?>> liveTypes()
		{
			return this.latestTypesPerName;
		}

		@Override
		public final synchronized boolean registerDefinitionEntry(final PersistenceTypeDefinition<?> typeDescription)
		{
			if(this.internalRegisterType2(typeDescription))
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public synchronized boolean registerDefinitionEntries(
			final XGettingCollection<? extends PersistenceTypeDefinition<?>> typeDescriptions
		)
		{
			final long oldSize = this.types.size();

			for(final PersistenceTypeDefinition<?> td : typeDescriptions)
			{
				this.internalRegisterType2(td);
			}

			if(this.types.size() != oldSize)
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public PersistenceTypeDefinition<?> lookupTypeByName(final String typeName)
		{
			return this.latestTypesPerName.get(typeName);
		}

		@Override
		public PersistenceTypeDefinition<?> lookupTypeById(final long typeId)
		{
			return this.allTypesPerTypeId.get(typeId);
		}

		@Override
		public long determineHighestTypeId()
		{
			long maxTypeId = -1;

			for(final PersistenceTypeDefinition<?> type : this.types)
			{
				if(type.typeId() >= maxTypeId)
				{
					maxTypeId = type.typeId();
				}
			}

			return maxTypeId;
		}

		@Override
		public String toString()
		{
			final VarString vc = VarString.New();

			for(final PersistenceTypeDefinition<?> type : this.types)
			{
				vc.add(type).lf();
			}

			return vc.toString();
		}

	}

}
