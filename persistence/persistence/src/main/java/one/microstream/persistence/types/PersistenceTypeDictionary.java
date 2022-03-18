package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistencyDictionary;
import one.microstream.reference.Swizzling;
import one.microstream.reflect.XReflect;
import one.microstream.typing.KeyValue;


public interface PersistenceTypeDictionary
{
	public PersistenceTypeDefinition lookupTypeByName(String typeName);

	public PersistenceTypeDefinition lookupTypeById(long typeId);
	
	
	public XGettingTable<String, ? extends PersistenceTypeLineage> typeLineages();

	public PersistenceTypeLineage lookupTypeLineage(Class<?> type);
	
	public PersistenceTypeLineage lookupTypeLineage(String typeName);
	
	
	public XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions();
	
	public boolean isEmpty();

	public long determineHighestTypeId();
	
	public PersistenceTypeDictionaryView view();
	
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
	
	public default <C extends Consumer<? super PersistenceTypeLineage>> C iterateTypeLineageViews(final C logic)
	{
		return this.typeLineages().values().iterate(logic);
	}

	
	// mutating logic //
	
	public PersistenceTypeLineage ensureTypeLineage(Class<?> type);
	
	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);

	public boolean registerTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);

	public boolean registerRuntimeTypeDefinition(PersistenceTypeDefinition typeDefinition);

	public boolean registerRuntimeTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);

	public PersistenceTypeDictionary setTypeDescriptionRegistrationObserver(PersistenceTypeDefinitionRegistrationObserver observer);

	public PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver();
	

	
	public static void validateTypeId(final PersistenceTypeDefinition typeDefinition)
	{
		if(Swizzling.isFoundId(typeDefinition.typeId()))
		{
			return;
		}
		
		throw new PersistenceException("Uninitialized TypeId for type definition " + typeDefinition.typeName());
	}
	
	public static void validateTypeIds(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
	{
		typeDefinitions.forEach(PersistenceTypeDictionary::validateTypeId);
	}
	
	
	public default <C extends Consumer<? super PersistenceTypeLineage>> C iterateTypeLineages(final C logic)
	{
		return this.typeLineages().values().iterate(logic);
	}

	public static <D extends PersistenceTypeDictionary> D registerTypes(
		final D                                                          typeDictionary  ,
		final XGettingCollection<? extends PersistenceTypeDefinition> typeDefinitions
	)
	{
		typeDictionary.registerTypeDefinitions(typeDefinitions);
		return typeDictionary;
	}
	
	public static VarString assembleTypesPerTypeId(
		final VarString                                      vs               ,
		final XGettingTable<Long, PersistenceTypeDefinition> allTypesPerTypeId
	)
	{
		for(final PersistenceTypeDefinition type : allTypesPerTypeId.values())
		{
			vs.add(type).lf();
		}
		
		return vs;
	}
	
	public static long determineHighestTypeId(final XGettingTable<Long, PersistenceTypeDefinition> allTypesPerTypeId)
	{
		long maxTypeId = Swizzling.notFoundId();

		for(final Long typeId : allTypesPerTypeId.keys())
		{
			if(typeId >= maxTypeId)
			{
				maxTypeId = typeId;
			}
		}

		return maxTypeId;
	}

	
	
	public static PersistenceTypeDictionary New(final PersistenceTypeLineageCreator typeLineageCreator)
	{
		return new PersistenceTypeDictionary.Default(
			notNull(typeLineageCreator)
		);
	}

	public static PersistenceTypeDictionary New(
		final PersistenceTypeLineageCreator                           typeLineageCreator,
		final XGettingCollection<? extends PersistenceTypeDefinition> typeDefinitions
	)
	{
		return PersistenceTypeDictionary.registerTypes(
			New(typeLineageCreator),
			typeDefinitions
		);
	}

	public final class Default implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// the dictionary must be enhanceable at runtime, hence it must know a type lineage provider
		private final PersistenceTypeLineageCreator                  typeLineageCreator;
		private final EqHashTable<String, PersistenceTypeLineage>    typeLineages       = EqHashTable.New();
		
		private final EqHashTable<Long  , PersistenceTypeDefinition> allTypesPerTypeId  = EqHashTable.New();
		private       PersistenceTypeDefinitionRegistrationObserver  registrationObserver;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceTypeLineageCreator typeLineageCreator)
		{
			super();
			this.typeLineageCreator = typeLineageCreator;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final XGettingTable<String, PersistenceTypeLineage> typeLineages()
		{
			return this.typeLineages;
		}
		
		@Override
		public synchronized PersistenceTypeLineage lookupTypeLineage(final Class<?> type)
		{
			return this.synchLookupTypeLineage(type.getName());
		}
		
		@Override
		public synchronized PersistenceTypeLineage lookupTypeLineage(final String typeName)
		{
			return this.synchLookupTypeLineage(typeName);
		}
		
		private <T> PersistenceTypeLineage synchLookupTypeLineage(final String typeName)
		{
			final PersistenceTypeLineage lineage = this.typeLineages.get(typeName);
			return lineage;
		}
		
		
		@Override
		public synchronized PersistenceTypeLineage ensureTypeLineage(final Class<?> type)
		{
			final PersistenceTypeLineage lineage = this.lookupTypeLineage(type);
			if(lineage != null)
			{
				return lineage;
			}
			
			return this.synchRegisterTypeLineage(this.typeLineageCreator.createTypeLineage(type));
		}
				
		private <T> PersistenceTypeLineage synchRegisterTypeLineage(final PersistenceTypeLineage lineage)
		{
			this.typeLineages.add(lineage.typeName(), lineage);
			this.synchSortTypeLineages();

			return lineage;
		}
		
		public synchronized PersistenceTypeLineage ensureTypeLineage(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeLineage typeLineage = this.lookupTypeLineage(
				typeDefinition.runtimeTypeName()
			);
			
			if(typeLineage == null)
			{
				typeLineage = this.typeLineageCreator.createTypeLineage(
					typeDefinition.runtimeTypeName(),
					typeDefinition.type()
				);
				this.synchRegisterTypeLineage(typeLineage);
			}

			return typeLineage;
		}

		final <T> boolean synchRegisterType(final PersistenceTypeDefinition typeDefinition)
		{
			final PersistenceTypeLineage lineage = this.ensureTypeLineage(typeDefinition);
				
			// may not abort here in order to consistently use TypeHandlers over dictionary-loaded definitions
			final boolean hasChanged = lineage.registerTypeDefinition(typeDefinition);
//			if(!lineage.registerTypeDefinition(typeDefinition))
//			{
//				// type definition already contained, abort.
//				return false;
//			}
			
			// definitions can be replaced by another instance (e.g. a plain instance by a handler instance)
			this.allTypesPerTypeId.put(typeDefinition.typeId(), typeDefinition);

			// callback gets set externally, can/may be null, so check for it.
			if(this.registrationObserver != null)
			{
				this.registrationObserver.observeTypeDefinitionRegistration(typeDefinition);
			}
			
			// the proper state feedback is important for avoiding redundant updats to the dictionary persistent form.
			return hasChanged;
//			return true;
		}
		
		private void synchSortTypeLineages()
		{
			this.typeLineages.keys().sort(XSort::compare);
		}

		private void internalSort()
		{
			this.allTypesPerTypeId.keys().sort(XSort::compare);
		}
		
		@Override
		public final synchronized PersistenceTypeDictionary setTypeDescriptionRegistrationObserver(
			final PersistenceTypeDefinitionRegistrationObserver registrationObserver
		)
		{
			this.registrationObserver = registrationObserver;
			
			return this;
		}

		@Override
		public final synchronized PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver()
		{
			return this.registrationObserver;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions()
		{
			return this.allTypesPerTypeId;
		}
		
		@Override
		public final synchronized boolean isEmpty()
		{
			return this.allTypesPerTypeId.isEmpty();
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeDictionary.validateTypeId(typeDefinition);
			
			if(this.synchRegisterType(typeDefinition))
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			// first validate all before changing any state
			PersistenceTypeDictionary.validateTypeIds(typeDefinitions);
			
			final long oldSize = this.allTypesPerTypeId.size();

			for(final PersistenceTypeDefinition td : typeDefinitions)
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
			final PersistenceTypeDefinition typeDefinition
		)
		{
			final boolean returnValue = this.registerTypeDefinition(typeDefinition);
			this.synchSetRuntimeTypeDefinition(typeDefinition);
			
			return returnValue;
		}
		
		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final boolean returnValue = this.registerTypeDefinitions(typeDefinitions);
			
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				this.synchSetRuntimeTypeDefinition(td);
			}
			
			return returnValue;
		}
		
		private <T> void synchSetRuntimeTypeDefinition(final PersistenceTypeDefinition td)
		{
			final PersistenceTypeLineage lineage = this.lookupTypeLineage(td.runtimeTypeName());
			lineage.setRuntimeTypeDefinition(td);
		}

		@Override
		public final synchronized PersistenceTypeDefinition lookupTypeByName(final String typeName)
		{
			final PersistenceTypeLineage lineage = this.lookupTypeLineage(typeName);
			
			return lineage == null
				? null
				: lineage.latest()
			;
		}

		@Override
		public final synchronized PersistenceTypeDefinition lookupTypeById(final long typeId)
		{
			return this.allTypesPerTypeId.get(typeId);
		}

		@Override
		public final synchronized long determineHighestTypeId()
		{
			return PersistenceTypeDictionary.determineHighestTypeId(this.allTypesPerTypeId);
		}
		
		@Override
		public synchronized PersistenceTypeDictionaryView view()
		{
			// wrap in an instance of an immutable view implementation
			return PersistenceTypeDictionaryView.New(this);
		}

		@Override
		public final synchronized String toString()
		{
			return PersistenceTypeDictionary.assembleTypesPerTypeId(VarString.New(), this.allTypesPerTypeId).toString();
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
		final String qualifier,
		final String fieldName
	)
	{
		if(qualifier == null)
		{
			return fieldName;
		}
		
		return fullQualifiedFieldName(VarString.New(), qualifier, fieldName).toString();
	}
	
	public static char fullQualifiedFieldNameSeparator()
	{
		return Symbols.MEMBER_FIELD_QUALIFIER_SEPERATOR;
	}

	public static VarString fullQualifiedFieldName(
		final VarString vc       ,
		final String    qualifier,
		final String    fieldName
	)
	{
		if(qualifier != null)
		{
			vc.add(qualifier).add(fullQualifiedFieldNameSeparator());
		}
		
		return vc.add(fieldName);
	}
	
	public static KeyValue<String, String> splitFullQualifiedFieldName(
		final String identifier
	)
	{
		final int index = identifier.lastIndexOf(fullQualifiedFieldNameSeparator());
		
		return index < 0
			? X.KeyValue(null, identifier)
			: X.KeyValue(identifier.substring(0, index).trim(), identifier.substring(index + 1).trim())
		;
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the appropriate member types
	public static boolean isInlinedComplexType(final String typeName)
	{
		return Symbols.TYPE_COMPLEX.equals(typeName);
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the appropriate member types
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
		protected static final transient char   MEMBER_FIELD_QUALIFIER_SEPERATOR = XReflect.fieldIdentifierDelimiter();
		protected static final transient char   MEMBER_TERMINATOR                = ','; // cannot be ";" as array names are terminated by it
		protected static final transient char   MEMBER_COMPLEX_DEF_START         = '(';
		protected static final transient char   MEMBER_COMPLEX_DEF_END           = ')';
		
		// (30.07.2019 TM)NOTE: literal parsing implemented but then not needed. Kept around a while.
//		protected static final transient char   LITERAL_DELIMITER                = '"';
//		protected static final transient char   LITERAL_ESCAPER                  = '\\';

		protected static final transient String KEYWORD_PRIMITIVE                = "primitive";
		protected static final transient String KEYWORD_ENUM                     = XReflect.typename_enum();
		protected static final transient String TYPE_CHARS                       = "[char]"   ;
		protected static final transient String TYPE_BYTES                       = "[byte]"   ;
		protected static final transient String TYPE_COMPLEX                     = "[list]"   ;

		protected static final transient char[] ARRAY_KEYWORD_PRIMITIVE          = KEYWORD_PRIMITIVE.toCharArray();
		protected static final transient char[] ARRAY_KEYWORD_ENUM               = KEYWORD_ENUM     .toCharArray();
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
