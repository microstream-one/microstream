package one.microstream.util.csv;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.chars.CsvParserCharArray;
import one.microstream.chars.StringTable;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XAddingEnum;
import one.microstream.collections.types.XAddingList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.util.Substituter;


public final class CsvContentBuilderCharArray implements CsvContent.Builder<_charArrayRange>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final CsvContentBuilderCharArray New()
	{
		return New(CSV.configurationDefault());
	}

	public static final CsvContentBuilderCharArray New(final CsvConfiguration csvConfiguration)
	{
		return new CsvContentBuilderCharArray(
			csvConfiguration,
			Substituter.<String>New(),
			CsvParserCharArray.New(),
			new StringTable.Default.Creator()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final CsvConfiguration           configuration;
	private final Substituter<String>        stringCache  ;
	private final CsvParser<_charArrayRange> parser       ;
	private final StringTable.Creator        tableCreator ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private CsvContentBuilderCharArray(
		final CsvConfiguration           configuration,
		final Substituter<String>        stringCache  ,
		final CsvParser<_charArrayRange> parser       ,
		final StringTable.Creator        tableCreator
	)
	{
		super();
		this.configuration = notNull(configuration);
		this.stringCache   = notNull(stringCache)  ;
		this.parser        = notNull(parser)       ;
		this.tableCreator  = notNull(tableCreator) ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public CsvContent build(final String name, final _charArrayRange medium)
	{
		final BulkList<StringTable> tables         = new BulkList<>();
		final TableCollector        tableCollector = new TableCollector(tables, this.tableCreator, this.stringCache);

		final CsvConfiguration effectiveConfig = this.parser.parseCsvData(this.configuration, medium, tableCollector);

		return CsvContent.Default.NewTranslated(name, tables, effectiveConfig);
	}



	public final class TableCollector implements CsvRowCollector
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
