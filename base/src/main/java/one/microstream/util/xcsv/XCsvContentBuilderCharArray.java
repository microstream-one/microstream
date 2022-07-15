package one.microstream.util.xcsv;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.chars.StringTable;
import one.microstream.chars.XCsvParserCharArray;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XAddingEnum;
import one.microstream.collections.types.XAddingList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.util.Substituter;


public final class XCsvContentBuilderCharArray implements XCsvContent.Builder<_charArrayRange>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final XCsvContentBuilderCharArray New()
	{
		return New(XCSV.configurationDefault());
	}
	
	public static final XCsvContentBuilderCharArray New(
		final XCsvDataType dataType
	)
	{
		return New(null, dataType);
	}

	public static final XCsvContentBuilderCharArray New(
		final XCsvConfiguration csvConfiguration
	)
	{
		return New(csvConfiguration, null);
	}
	
	public static final XCsvContentBuilderCharArray New(
		final XCsvConfiguration csvConfiguration,
		final XCsvDataType      dataType
	)
	{
		return new XCsvContentBuilderCharArray(
			mayNull(dataType),
			mayNull(csvConfiguration),
			Substituter.<String>New(),
			XCsvParserCharArray.New(),
			new StringTable.Default.Creator()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCsvDataType                dataType     ;
	private final XCsvConfiguration           configuration;
	private final Substituter<String>         stringCache  ;
	private final XCsvParser<_charArrayRange> parser       ;
	private final StringTable.Creator         tableCreator ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private XCsvContentBuilderCharArray(
		final XCsvDataType                dataType     ,
		final XCsvConfiguration           configuration,
		final Substituter<String>         stringCache  ,
		final XCsvParser<_charArrayRange> parser       ,
		final StringTable.Creator         tableCreator
	)
	{
		super();
		this.dataType      = dataType     ;
		this.configuration = configuration;
		this.stringCache   = stringCache  ;
		this.parser        = parser       ;
		this.tableCreator  = tableCreator ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XCsvContent build(final String name, final _charArrayRange data)
	{
		final BulkList<StringTable> tables         = BulkList.New();
		final TableCollector        tableCollector = new TableCollector(tables, this.tableCreator, this.stringCache);

		final XCsvConfiguration effectiveConfig = this.parser.parseCsvData(
			this.dataType,
			this.configuration,
			data,
			tableCollector
		);

		return XCsvContent.Default.NewTranslated(name, tables, effectiveConfig);
	}



	public final class TableCollector implements XCsvRowCollector
	{
		private final Substituter<String>    stringCache   ;
		private final Consumer<StringTable> tableCollector;
		private final StringTable.Creator    tableCreator  ;

		private final BulkList<String[]>     rows;
		private final BulkList<String>       row ;

		private String                       tableName  ;
		private XGettingSequence<String>     columnNames;
		private XGettingList<String>         columnTypes;


		public TableCollector(
			final Consumer<StringTable> tableCollector,
			final StringTable.Creator    tableCreator  ,
			final Substituter<String>    stringCache
		)
		{
			super();
			this.stringCache    = notNull(stringCache)   ;
			this.tableCollector = notNull(tableCollector);
			this.tableCreator   = notNull(tableCreator)  ;
			this.rows           = new BulkList<>()       ;
			this.row            = new BulkList<>()       ;
		}

		@Override
		public void beginTable(
			final String                   tableName  ,
			final XGettingSequence<String> columnNames,
			final XGettingList<String>     columnTypes
		)
		{
			this.tableName   = tableName  ;
			this.columnNames = columnNames;
			this.columnTypes = columnTypes;
		}

		@Override
		public final void accept(final char[] data, final int offset, final int length)
		{
			this.row.add(this.stringCache.substitute(data == null ? null : new String(data, offset, length)));
		}

		@Override
		public final void completeRow()
		{
			if(this.row.isEmpty())
			{
				// either already completed or data ended after a line separator
				return;
			}
			this.rows.add(this.row.toArray(String.class));
			this.row.clear();
		}

		@Override
		public final void completeTable()
		{
			this.tableCollector.accept(
				this.tableCreator.createStringTable(
					this.tableName  ,
					this.columnNames,
					this.columnTypes,
					this.rows
				)
			);
			this.row.clear();
			this.rows.clear();
		}

	}

	static final class ColumnNamesCollector implements Consumer<String>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XAddingEnum<String> columnNames;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public ColumnNamesCollector(final XAddingEnum<String> columnNames)
		{
			super();
			this.columnNames = notNull(columnNames);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final String columnName)
		{
			this.columnNames.add(columnName);
		}

	}

	static final class ColumnTypesCollector implements Consumer<String>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XAddingList<String> columnTypes;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public ColumnTypesCollector(final XAddingList<String> columnTypes)
		{
			super();
			this.columnTypes = notNull(columnTypes);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final String columnName)
		{
			this.columnTypes.add(columnName);
		}

	}

}
