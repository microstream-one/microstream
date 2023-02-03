package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.X;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingMap;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.typing.KeyValue;
import one.microstream.util.xcsv.XCSV;
import one.microstream.util.xcsv.XCsvConfiguration;

public interface StorageDataConverterCsvConfiguration
{
	public XCsvConfiguration csvConfiguration();

	public XGettingMap<String, String> typeNameToCsvTypeNameMapping();

	public XGettingMap<String, String> csvTypeNameToActualTypeNameMapping();

	public String literalBooleanTrue();

	public String literalBooleanFalse();

	public char controlCharacterSeparator();

	public String objectIdColumnName();

	public String objectIdColumnTypeName();

	public String referenceTypeName();

	public char literalListStarter();

	public char literalListTerminator();

	public char literalListSeparator();

	public default String resolveActualTypeName(final String csvTypeName)
	{
		return X.coalesce(this.csvTypeNameToActualTypeNameMapping().get(csvTypeName), csvTypeName);
	}



	public static XGettingMap<String, String> createTypeNameToCsvTypeNameMapping(
		final String typeNameChars  ,
		final String typeNameBinary ,
		final String typeNameComplex
	)
	{
		final EqConstHashTable<String, String> map = EqConstHashTable.New(
			Defaults.transientEntry(byte   .class                                                   ),
			Defaults.transientEntry(boolean.class                                                   ),
			Defaults.transientEntry(short  .class                                                   ),
			Defaults.transientEntry(char   .class                                                   ),
			Defaults.transientEntry(int    .class                                                   ),
			Defaults.transientEntry(float  .class                                                   ),
			Defaults.transientEntry(long   .class                                                   ),
			Defaults.transientEntry(double .class                                                   ),
			Defaults.mappedEntry   (PersistenceTypeDictionary.Symbols.typeChars()  , typeNameChars  ),
			Defaults.mappedEntry   (PersistenceTypeDictionary.Symbols.typeBytes()  , typeNameBinary ),
			Defaults.mappedEntry   (PersistenceTypeDictionary.Symbols.typeComplex(), typeNameComplex)
		);
		return map;
	}

	public static XGettingMap<String, String> deriveTypeNameToCsvTypeNameMapping(
		final XGettingMap<String, String> typeNameToCsvTypeNameMapping
	)
	{
		final EqHashTable<String, String> csvTypeNameToTypeNameMapping = EqHashTable.New();
		for(final KeyValue<String, String> entry : typeNameToCsvTypeNameMapping)
		{
			csvTypeNameToTypeNameMapping.add(entry.value(), entry.key());
		}
		if(csvTypeNameToTypeNameMapping.size() != typeNameToCsvTypeNameMapping.size())
		{
			throw new StorageException("Ambiguous csv type mapping entries.");
		}


		return csvTypeNameToTypeNameMapping;
	}


	public static Builder Builder()
	{
		return new Builder.Default();
	}


	public static StorageDataConverterCsvConfiguration New(
		final XCsvConfiguration           csvConfiguration            ,
		final XGettingMap<String, String> typeNameToCsvTypeNameMapping,
		final XGettingMap<String, String> csvTypeNameToTypeNameMapping,
		final String                      literalBooleanTrue          ,
		final String                      literalBooleanFalse         ,
		final char                        controlCharacterSeparator   ,
		final String                      objectIdColumnName          ,
		final String                      objectIdColumnTypeName      ,
		final String                      referenceTypeName           ,
		final char                        literalListStarter          ,
		final char                        literalListTerminator       ,
		final char                        literalListSeparator
	)
	{
		return new StorageDataConverterCsvConfiguration.Default(
			notNull(csvConfiguration)            ,
			notNull(typeNameToCsvTypeNameMapping),
			notNull(csvTypeNameToTypeNameMapping),
			notNull(literalBooleanTrue)          ,
			notNull(literalBooleanFalse)         ,
			        controlCharacterSeparator    ,
			notNull(objectIdColumnName)          ,
			notNull(objectIdColumnTypeName)      ,
			notNull(referenceTypeName)           ,
			        literalListStarter           ,
			        literalListTerminator        ,
			        literalListSeparator
		);
	}
	
	

	public final class Default implements StorageDataConverterCsvConfiguration
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XCsvConfiguration            csvConfiguration            ;
		final XGettingMap<String, String> typeNameToCsvTypeNameMapping;
		final XGettingMap<String, String> csvTypeNameToTypeNameMapping;
		final String                      literalBooleanTrue          ;
		final String                      literalBooleanFalse         ;
		final char                        controlCharacterSeparator   ;
		final String                      objectIdColumnName          ;
		final String                      objectIdColumnTypeName      ;
		final String                      referenceTypeName           ;
		final char                        literalListStarter          ;
		final char                        literalListTerminator       ;
		final char                        literalListSeparator        ;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final XCsvConfiguration           csvConfiguration            ,
			final XGettingMap<String, String> typeNameToCsvTypeNameMapping,
			final XGettingMap<String, String> csvTypeNameToTypeNameMapping,
			final String                      literalBooleanTrue          ,
			final String                      literalBooleanFalse         ,
			final char                        controlCharacterSeparator   ,
			final String                      objectIdColumnName          ,
			final String                      objectIdColumnTypeName      ,
			final String                      referenceTypeName           ,
			final char                        literalListStarter          ,
			final char                        literalListTerminator       ,
			final char                        literalListSeparator
		)
		{
			super();
			this.csvConfiguration             = csvConfiguration            ;
			this.typeNameToCsvTypeNameMapping = typeNameToCsvTypeNameMapping;
			this.csvTypeNameToTypeNameMapping = csvTypeNameToTypeNameMapping;
			this.literalBooleanTrue           = literalBooleanTrue          ;
			this.literalBooleanFalse          = literalBooleanFalse         ;
			this.controlCharacterSeparator    = controlCharacterSeparator   ;
			this.objectIdColumnName           = objectIdColumnName          ;
			this.objectIdColumnTypeName       = objectIdColumnTypeName      ;
			this.referenceTypeName            = referenceTypeName           ;
			this.literalListStarter           = literalListStarter          ;
			this.literalListTerminator        = literalListTerminator       ;
			this.literalListSeparator         = literalListSeparator        ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XCsvConfiguration csvConfiguration()
		{
			return this.csvConfiguration;
		}

		@Override
		public final XGettingMap<String, String> typeNameToCsvTypeNameMapping()
		{
			return this.typeNameToCsvTypeNameMapping;
		}

		@Override
		public final XGettingMap<String, String> csvTypeNameToActualTypeNameMapping()
		{
			return this.csvTypeNameToTypeNameMapping;
		}

		@Override
		public final String literalBooleanTrue()
		{
			return this.literalBooleanTrue;
		}

		@Override
		public final String literalBooleanFalse()
		{
			return this.literalBooleanFalse;
		}

		@Override
		public final char controlCharacterSeparator()
		{
			return this.controlCharacterSeparator;
		}

		@Override
		public final String objectIdColumnName()
		{
			return this.objectIdColumnName;
		}

		@Override
		public final String objectIdColumnTypeName()
		{
			return this.objectIdColumnTypeName;
		}

		@Override
		public final String referenceTypeName()
		{
			return this.referenceTypeName;
		}

		@Override
		public final char literalListStarter()
		{
			return this.literalListStarter;
		}

		@Override
		public final char literalListTerminator()
		{
			return this.literalListTerminator;
		}

		@Override
		public final char literalListSeparator()
		{
			return this.literalListSeparator;
		}

	}

	public static StorageDataConverterCsvConfiguration defaultConfiguration()
	{
		return new StorageDataConverterCsvConfiguration.Defaults();
	}

	// (08.05.2019 TM)TODO: This should rather be an interface "Defaults" to just statically encapsulate default values
	public final class Defaults implements StorageDataConverterCsvConfiguration
	{
		static final String DEFAULT_LITERAL_BOOLEAN_TRUE        = "t"        ;
		static final String DEFAULT_LITERAL_BOOLEAN_FALSE       = "f"        ;
		static final char   DEFAULT_CONTROL_CHARACTER_SEPARATOR = ';'        ;
		static final char   DEFAULT_LITERAL_LIST_STARTER        = '['        ;
		static final char   DEFAULT_LITERAL_LIST_TERMINATOR     = ']'        ;
		static final char   DEFAULT_LITERAL_LIST_SEPARATOR      = ','        ;
		static final String DEFAULT_TYPE_NAME_CHARS             = "string"   ;
		static final String DEFAULT_TYPE_NAME_BYTES             = "binary"   ;
		static final String DEFAULT_TYPE_NAME_CMPLX             = "complex"  ;
		static final String DEFAULT_TYPE_NAME_REFERENCE         = "reference";

		static final XGettingMap<String, String> DEFAULT_TYPE_MAPPING =
			StorageDataConverterCsvConfiguration.createTypeNameToCsvTypeNameMapping(
				DEFAULT_TYPE_NAME_CHARS,
				DEFAULT_TYPE_NAME_BYTES,
				DEFAULT_TYPE_NAME_CMPLX
			)
		;

		static final XGettingMap<String, String> DEFAULT_TYPE_MAPPING_REVERSED =
			StorageDataConverterCsvConfiguration.deriveTypeNameToCsvTypeNameMapping(
				DEFAULT_TYPE_MAPPING
			)
		;

		static final KeyValue<String, String> transientEntry(final Class<?> type)
		{
			return X.KeyValue(type.getName(), type.getName());
		}

		static final KeyValue<String, String> mappedEntry(final String key, final String value)
		{
			return X.KeyValue(key, value);
		}



		@Override
		public final XCsvConfiguration csvConfiguration()
		{
			return XCSV.configurationDefault();
		}

		@Override
		public final XGettingMap<String, String> typeNameToCsvTypeNameMapping()
		{
			return DEFAULT_TYPE_MAPPING;
		}

		@Override
		public final XGettingMap<String, String> csvTypeNameToActualTypeNameMapping()
		{
			return DEFAULT_TYPE_MAPPING_REVERSED;
		}

		@Override
		public final String literalBooleanTrue()
		{
			return DEFAULT_LITERAL_BOOLEAN_TRUE;
		}

		@Override
		public final String literalBooleanFalse()
		{
			return DEFAULT_LITERAL_BOOLEAN_FALSE;
		}

		@Override
		public final char controlCharacterSeparator()
		{
			return DEFAULT_CONTROL_CHARACTER_SEPARATOR;
		}

		@Override
		public final String objectIdColumnName()
		{
			return Persistence.objectIdLabel();
		}

		@Override
		public final String objectIdColumnTypeName()
		{
			return Persistence.objectIdType().getName(); // avoids substring of getSimpleName() and yields same result
		}

		@Override
		public final String referenceTypeName()
		{
			return DEFAULT_TYPE_NAME_REFERENCE;
		}

		@Override
		public final char literalListStarter()
		{
			return DEFAULT_LITERAL_LIST_STARTER;
		}

		@Override
		public final char literalListTerminator()
		{
			return DEFAULT_LITERAL_LIST_TERMINATOR;
		}

		@Override
		public final char literalListSeparator()
		{
			return DEFAULT_LITERAL_LIST_SEPARATOR;
		}

	}


	public interface Builder
	{
		public XCsvConfiguration csvConfiguration();

		public XGettingMap<String, String> typeNameToCsvTypeNameMapping();

		public XGettingMap<String, String> csvTypeNameToTypeNameMapping();

		public String literalBooleanTrue();

		public String literalBooleanFalse();

		public char controlCharacterSeparator();

		public String objectIdColumnName();

		public String objectIdColumnTypeName();

		public String referenceTypeName();

		public char literalListStarter();

		public char literalListTerminator();

		public char literalListSeparator();

		public Builder csvConfiguration(XCsvConfiguration csvConfiguration);

		public Builder typeNameToCsvTypeNameMapping(XGettingMap<String, String> typeNameToCsvTypeNameMapping);

		public Builder csvTypeNameToTypeNameMapping(XGettingMap<String, String> csvTypeNameToTypeNameMapping);

		public Builder literalBooleanTrue(String literalBooleanTrue);

		public Builder literalBooleanFalse(String literalBooleanFalse);

		public Builder controlCharacterSeparator(char controlCharacterSeparator);

		public Builder objectIdColumnName(String objectIdColumnName);

		public Builder objectIdColumnTypeName(String objectIdColumnTypeName);

		public Builder referenceTypeName(String referenceTypeName);

		public Builder literalListStarter(char literalListStarter);

		public Builder literalListTerminator(char literalListTerminator);

		public Builder literalListSeparator(char literalListSeparator);

		public Builder reset();

		public StorageDataConverterCsvConfiguration build();



		public final class Default implements Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			XCsvConfiguration           csvConfiguration            ;
			XGettingMap<String, String> typeNameToCsvTypeNameMapping;
			XGettingMap<String, String> csvTypeNameToTypeNameMapping;
			String                      literalBooleanTrue          ;
			String                      literalBooleanFalse         ;
			char                        controlCharacterSeparator   ;
			String                      objectIdColumnName          ;
			String                      objectIdColumnTypeName      ;
			String                      referenceTypeName           ;
			char                        literalListStarter          ;
			char                        literalListTerminator       ;
			char                        literalListSeparator        ;

			

			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default()
			{
				super();
				this.reset(); // centralized way to initialize and reset values.
			}


			///////////////////////////////////////////////////////////////////////////
			// declared methods //
			/////////////////////

			final void validate()
			{
				// (26.09.2014 TM)TODO: StorageDataConverterCsvConfiguration.Builder.Default.validate()
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public final Builder reset()
			{
				final StorageDataConverterCsvConfiguration defaultConfiguration = defaultConfiguration();
				this.csvConfiguration             = defaultConfiguration.csvConfiguration()            ;
				this.typeNameToCsvTypeNameMapping = defaultConfiguration.typeNameToCsvTypeNameMapping();
				this.csvTypeNameToTypeNameMapping = defaultConfiguration.csvTypeNameToActualTypeNameMapping();
				this.literalBooleanTrue           = defaultConfiguration.literalBooleanTrue()          ;
				this.literalBooleanFalse          = defaultConfiguration.literalBooleanFalse()         ;
				this.controlCharacterSeparator    = defaultConfiguration.controlCharacterSeparator()   ;
				this.objectIdColumnName           = defaultConfiguration.objectIdColumnName()          ;
				this.objectIdColumnTypeName       = defaultConfiguration.objectIdColumnTypeName()      ;
				this.referenceTypeName            = defaultConfiguration.referenceTypeName()           ;
				this.literalListStarter           = defaultConfiguration.literalListStarter()          ;
				this.literalListTerminator        = defaultConfiguration.literalListTerminator()       ;
				this.literalListSeparator         = defaultConfiguration.literalListSeparator()        ;
				return this;
			}

			@Override
			public final XCsvConfiguration csvConfiguration()
			{
				return this.csvConfiguration;
			}

			@Override
			public final XGettingMap<String, String> typeNameToCsvTypeNameMapping()
			{
				return this.typeNameToCsvTypeNameMapping;
			}

			@Override
			public final XGettingMap<String, String> csvTypeNameToTypeNameMapping()
			{
				return this.csvTypeNameToTypeNameMapping;
			}

			@Override
			public final String literalBooleanTrue()
			{
				return this.literalBooleanTrue;
			}

			@Override
			public final String literalBooleanFalse()
			{
				return this.literalBooleanFalse;
			}

			@Override
			public final char controlCharacterSeparator()
			{
				return this.controlCharacterSeparator;
			}

			@Override
			public final String objectIdColumnName()
			{
				return this.objectIdColumnName;
			}

			@Override
			public final String objectIdColumnTypeName()
			{
				return this.objectIdColumnTypeName;
			}

			@Override
			public final String referenceTypeName()
			{
				return this.referenceTypeName;
			}

			@Override
			public final char literalListStarter()
			{
				return this.literalListStarter;
			}

			@Override
			public final char literalListTerminator()
			{
				return this.literalListTerminator;
			}

			@Override
			public final char literalListSeparator()
			{
				return this.literalListSeparator;
			}

			@Override
			public final Builder csvConfiguration(final XCsvConfiguration csvConfiguration)
			{
				this.csvConfiguration = csvConfiguration;
				return this;
			}

			@Override
			public final Builder typeNameToCsvTypeNameMapping(
				final XGettingMap<String, String> typeNameToCsvTypeNameMapping
			)
			{
				this.typeNameToCsvTypeNameMapping = typeNameToCsvTypeNameMapping;
				return this;
			}

			@Override
			public final Builder csvTypeNameToTypeNameMapping(
				final XGettingMap<String, String> csvTypeNameToTypeNameMapping
			)
			{
				this.csvTypeNameToTypeNameMapping = csvTypeNameToTypeNameMapping;
				return this;
			}

			@Override
			public final Builder literalBooleanTrue(final String literalBooleanTrue)
			{
				this.literalBooleanTrue = literalBooleanTrue;
				return this;
			}

			@Override
			public final Builder literalBooleanFalse(final String literalBooleanFalse)
			{
				this.literalBooleanFalse = literalBooleanFalse;
				return this;
			}

			@Override
			public final Builder controlCharacterSeparator(final char controlCharacterSeparator)
			{
				this.controlCharacterSeparator = controlCharacterSeparator;
				return this;
			}

			@Override
			public final Builder objectIdColumnName(final String objectIdColumnName)
			{
				this.objectIdColumnName = objectIdColumnName;
				return this;
			}

			@Override
			public final Builder objectIdColumnTypeName(final String objectIdColumnTypeName)
			{
				this.objectIdColumnTypeName = objectIdColumnTypeName;
				return this;
			}

			@Override
			public final Builder referenceTypeName(final String referenceTypeName)
			{
				this.referenceTypeName = referenceTypeName;
				return this;
			}

			@Override
			public final Builder literalListStarter(final char literalListStarter)
			{
				this.literalListStarter = literalListStarter;
				return this;
			}

			@Override
			public final Builder literalListTerminator(final char literalListTerminator)
			{
				this.literalListTerminator = literalListTerminator;
				return this;
			}

			@Override
			public final Builder literalListSeparator(final char literalListSeparator)
			{
				this.literalListSeparator = literalListSeparator;
				return this;
			}

			@Override
			public final StorageDataConverterCsvConfiguration build()
			{
				this.validate();
				return StorageDataConverterCsvConfiguration.New(
					this.csvConfiguration,
					this.typeNameToCsvTypeNameMapping,
					this.csvTypeNameToTypeNameMapping,
					this.literalBooleanTrue,
					this.literalBooleanFalse,
					this.controlCharacterSeparator,
					this.objectIdColumnName,
					this.objectIdColumnTypeName,
					this.referenceTypeName,
					this.literalListStarter,
					this.literalListTerminator,
					this.literalListSeparator
				);
			}

		}

	}

}
