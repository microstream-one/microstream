package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Function;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.SwizzleTypeDictionary;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDictionary extends SwizzleTypeDictionary
{
	public XGettingTable<Long, PersistenceTypeDescription<?>> allTypes();
	
	public XGettingTable<String, PersistenceTypeDescription<?>> currentTypesByName();
	
	public XGettingTable<Long, PersistenceTypeDescription<?>> currentTypesById();
	
	public XGettingTable<String, PersistenceTypeDescriptionLineage<?>> types();

	public boolean registerType(PersistenceTypeDescription<?> typeDescription);

	public boolean registerTypes(Iterable<? extends PersistenceTypeDescription<?>> typeDescriptions);

	@Override
	public PersistenceTypeDescription<?> lookupTypeByName(String typeName);

	@Override
	public PersistenceTypeDescription<?> lookupTypeById(long typeId);

	public long determineHighestTypeId();

	public void setTypeDescriptionRegistrationCallback(PersistenceTypeDescriptionRegistrationCallback callback);

	public PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback();


	public static PersistenceTypeDictionary New(final PersistenceTypeDescriptionLineageProvider lineageProvider)
	{
		return new PersistenceTypeDictionary.Implementation(
			notNull(lineageProvider)
		);
	}


	public final class Implementation implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDescriptionLineageProvider                 lineageProvider ;
		private final EqHashTable<Long  , PersistenceTypeDescription<?>>        typesPerTypeId   = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDescription<?>>        typesPerTypeName = EqHashTable.New();
		private final EqHashTable<String, PersistenceTypeDescriptionLineage<?>> lineages         = EqHashTable.New();
		private       PersistenceTypeDescriptionRegistrationCallback            callback        ;
		
		private final Function<String, PersistenceTypeDescriptionLineage<?>> lineageEnsurer;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final PersistenceTypeDescriptionLineageProvider lineageProvider)
		{
			super();
			this.lineageProvider = lineageProvider    ;
			this.lineageEnsurer  = this::ensureLineage;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		private <T> PersistenceTypeDescriptionLineage<T> ensureLineage(final String typeName)
		{
			return this.lineageProvider.provideTypeDescriptionLineage(typeName);
		}
		
		private synchronized <T> PersistenceTypeDescriptionLineage<T> ensureLineage(
			final PersistenceTypeDescription<T> typeDescription
		)
		{
			// type safety ensured by type name: the type parameter T always represents the type with the given name.
			@SuppressWarnings("unchecked")
			final
			PersistenceTypeDescriptionLineage<T> lineage = (PersistenceTypeDescriptionLineage<T>)this.lineages.ensure(
				typeDescription.typeName(),
				this.lineageEnsurer
			);
			
			return lineage;
		}

		final <T> boolean internalRegisterType(final PersistenceTypeDescription<T> typeDescription)
		{
			// type safety ensured by type name: the type parameter T always represents the type with the given name.
			@SuppressWarnings("unchecked")
			final
			PersistenceTypeDescriptionLineage<T> lineage = (PersistenceTypeDescriptionLineage<T>)this.lineages.ensure(
				typeDescription.typeName(),
				this.lineageEnsurer
			);
			
			lineage.register(typeDescription);
			
			
			if(!this.typesPerTypeId.add(typeDescription.typeId(), typeDescription))
			{
				return false;
			}
			
			if(!typeDescription.isCurrent())
			{
				this.typesPerTypeName.put(typeDescription.typeName(), typeDescription);
			}

			// callback gets set externally, can be null as well, so it must be checked.
			if(this.callback != null)
			{
				this.callback.registerTypeDescription(typeDescription);
			}
			
			return true;
		}

		private void internalSort()
		{
			SwizzleTypeIdOwner.sortByTypeIdAscending(this.typesPerTypeId.values());
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public void setTypeDescriptionRegistrationCallback(final PersistenceTypeDescriptionRegistrationCallback callback)
		{
			this.callback = callback;
		}

		@Override
		public PersistenceTypeDescriptionRegistrationCallback getTypeDescriptionRegistrationCallback()
		{
			return this.callback;
		}

		@Override
		public XGettingTable<Long, PersistenceTypeDescription<?>> allTypes()
		{
			return this.typesPerTypeId;
		}
		
		@Override
		public XGettingTable<String, PersistenceTypeDescription<?>> currentTypesByName()
		{
			return this.typesPerTypeName;
		}

		@Override
		public final synchronized boolean registerType(final PersistenceTypeDescription<?> typeDescription)
		{
			if(this.internalRegisterType(typeDescription))
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public synchronized boolean registerTypes(
			final XGettingCollection<? extends PersistenceTypeDescription<?>> typeDescriptions
		)
		{
			final long oldSize = this.typesPerTypeId.size();

			for(final PersistenceTypeDescription<?> td : typeDescriptions)
			{
				this.internalRegisterType(td);
			}

			if(this.typesPerTypeId.size() != oldSize)
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public PersistenceTypeDescription<?> lookupTypeByName(final String typeName)
		{
			return this.typesPerTypeName.get(typeName);
		}

		@Override
		public PersistenceTypeDescription<?> lookupTypeById(final long typeId)
		{
			return this.typesPerTypeId.get(typeId);
		}

		@Override
		public long determineHighestTypeId()
		{
			long maxTypeId = -1;
			
			for(final PersistenceTypeDescription<?> type : this.typesPerTypeId.values())
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

			for(final PersistenceTypeDescription<?> type : this.typesPerTypeId.values())
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
		final String    declaringTypeName         ,
		final int       maxDeclaringTypeNameLength,
		final String    fieldName                 ,
		final int       maxFieldNameLength
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
