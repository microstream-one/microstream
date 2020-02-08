package one.microstream.chars;

import static one.microstream.math.XMath.notNegative;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;
import one.microstream.collections.ConstList;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableList;
import one.microstream.typing.XTypes;
import one.microstream.util.xcsv.XCSV;
import one.microstream.util.xcsv.XCsvConfiguration;
import one.microstream.util.xcsv.XCsvContent;
import one.microstream.util.xcsv.XCsvContentBuilderCharArray;

public interface StringTable
{

	/**
	 * An arbitrary name identifying this table instance, potentially {@code null}.
	 *
	 * @return this table's name.
	 */
	public String name();

	public XGettingEnum<String> columnNames();

	public XGettingList<String> columnTypes();

	public XGettingList<String[]> rows();
	
	public <C extends BiConsumer<String, String>> C mapTo(
		C                          target     ,
		Function<String[], String> keyMapper  ,
		Function<String[], String> valueMapper
	);



	public interface Creator
	{
		public StringTable createStringTable(
			String                   name       ,
			XGettingSequence<String> columnNames,
			XGettingList<String>     columnTypes,
			XGettingList<String[]>   rows
		);
	}



	public final class Static
	{
		public static StringTable parse(final String rawData)
		{
			return parse(rawData, null, null);
		}
		
		public static StringTable parse(final _charArrayRange rawData)
		{
			return parse(rawData, null, null);
		}
		
		public static StringTable parse(
			final String        rawData ,
			final XCSV.DataType dataType
		)
		{
			return parse(rawData, null, dataType);
		}
		
		public static StringTable parse(
			final _charArrayRange rawData ,
			final XCSV.DataType   dataType
		)
		{
			return parse(rawData, null, dataType);
		}
		
		public static StringTable parse(final String rawData, final char valueSeparator)
		{
			return parse(rawData, XCsvConfiguration.New(valueSeparator), null);
		}
		
		public static StringTable parse(final _charArrayRange rawData, final char valueSeparator)
		{
			return parse(rawData, XCsvConfiguration.New(valueSeparator), null);
		}
		
		public static StringTable parse(
			final String            rawData         ,
			final XCsvConfiguration csvConfiguration,
			final XCSV.DataType     dataType
		)
		{
			/*
			 * can't copy around data all the time just because the JDK guys don't know how to write proper APIs
			 * (e.g. give String an iterate(_charConsumer) method so that logic could be written reusable)
			 * Or even better: make immutable arrays or optionally read-only accessible. But nooo...
			 */
			return parse(_charArrayRange.New(XChars.readChars(rawData)), csvConfiguration, dataType);
		}
		
		public static StringTable parse(
			final _charArrayRange   rawData         ,
			final XCsvConfiguration csvConfiguration,
			final XCSV.DataType     dataType
		)
		{
			final XCsvContentBuilderCharArray parser = XCsvContentBuilderCharArray.New(
				csvConfiguration, dataType
			);
			
			final XCsvContent content = parser.build(null, rawData);
			final StringTable data    = content.segments().first().value();

			return data;
		}
		
		// float because float to int conversion is automatically capped at max int.
		public static final int estimatedCharCountPerRow()
		{
			return 100;
		}
		
		public static final int calculateEstimatedCharCount(final long rowCount)
		{
			final long estimate = rowCount * estimatedCharCountPerRow();
			
			return estimate >= Integer.MAX_VALUE
				? Integer.MAX_VALUE
				: (int)estimate
			;
		}

		public static final String assembleString(final StringTable st)
		{
			return assembleString(VarString.New(calculateEstimatedCharCount(st.rows().size())), st).toString();
		}

		public static final VarString assembleString(final VarString vs, final StringTable st)
		{
			return assembleString(vs, st, null);
		}
		
		private static void assemble(final VarString vs, final char separator, final String[] elements)
		{
			if(elements.length == 0)
			{
				return;
			}
			
			for(final String s : elements)
			{
				vs.add(s).add(separator);
			}
			vs.deleteLast();
		}
		
		private static void assemble(final VarString vs, final char separator, final XGettingCollection<String> elements)
		{
			if(elements.isEmpty())
			{
				return;
			}
			
			for(final String s : elements)
			{
				vs.add(s).add(separator);
			}
			vs.deleteLast();
		}
		
		// (08.05.2017 TM)NOTE: centralized method to guarantee parser and assembler behave consistently
		private static XCsvConfiguration ensureCsvConfiguration(final XCsvConfiguration csvConfiguration)
		{
			return csvConfiguration == null
				? XCSV.configurationDefault()
				: csvConfiguration
			;
		}
		
		public static final VarString assembleString(
			final VarString        vs              ,
			final StringTable      st              ,
			final XCsvConfiguration csvConfiguration
		)
		{
			if(st.columnNames().isEmpty())
			{
				// column names are mandatory. So no columns means no data, even if there should be rows present.
				return vs;
				
				// (08.05.2017 TM)NOTE: can't just return a random string because it is not recognized by the parser.
//				return vs.add("[empty table]");
			}
			
			final XCsvConfiguration effConfig       = ensureCsvConfiguration(csvConfiguration);
			final char             valueSeparator  = effConfig.valueSeparator();
			final char             recordSeparator = effConfig.recordSeparator();

			// assemble column names
			assemble(vs, valueSeparator, st.columnNames());

			// assemble column types if present
			if(!st.columnTypes().isEmpty())
			{
				vs.add(recordSeparator).add('(');
				assemble(vs, valueSeparator, st.columnTypes());
				vs.add(')');
			}

			// assemble data rows if present
			if(!st.rows().isEmpty())
			{
				for(final String[] row : st.rows())
				{
					assemble(vs.add(recordSeparator), valueSeparator, row);
				}
			}

			return vs;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}



	public final class Default implements StringTable
	{
		public static final class Creator implements StringTable.Creator
		{

			@Override
			public StringTable createStringTable(
				final String                   name       ,
				final XGettingSequence<String> columnNames,
				final XGettingList<String>     columnTypes,
				final XGettingList<String[]>   rows
			)
			{
				return new StringTable.Default(name, columnNames, columnTypes, rows);
			}

		}


		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static void validateColumnCount(final int columnCount, final XGettingList<String[]> rows)
		{
			final long columnCountMismatchIndex = rows.scan(new ColumnCountValidator(columnCount));
			if(columnCountMismatchIndex >= 0)
			{
				// (01.07.2013 TM)EXCP: proper exception
				throw new IllegalArgumentException(
					"Invalid column count in row " + columnCountMismatchIndex
					+ " (" + columnCount + " required, " + rows.at(columnCountMismatchIndex).length + " available)"
				);
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                  name   ;
		private final EqConstHashEnum<String> columns;
		private final ConstList<String>       types  ;
		private final ConstList<String[]>     rows   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final XGettingSequence<String> columns    ,
			final XGettingList<String>     columnTypes,
			final XGettingList<String[]>   rows
		)
		{
			this(null, columns, columnTypes, rows);
		}

		public Default(
			final String                   name       ,
			final XGettingSequence<String> columns    ,
			final XGettingList<String>     columnTypes,
			final XGettingList<String[]>   rows
		)
		{
			super();
			this.name    = name                 ; // may be null
			this.columns = EqConstHashEnum.New(columns);
			validateColumnCount(XTypes.to_int(this.columns.size()), rows);
			this.types   = ConstList.New(columnTypes);
			this.rows    = ConstList.New(rows);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final XImmutableEnum<String> columnNames()
		{
			return this.columns;
		}

		@Override
		public final XGettingList<String> columnTypes()
		{
			return this.types;
		}

		@Override
		public final XImmutableList<String[]> rows()
		{
			return this.rows;
		}
		
		@Override
		public <C extends BiConsumer<String, String>> C mapTo(
			final C                          target     ,
			final Function<String[], String> keyMapper  ,
			final Function<String[], String> valueMapper
		)
		{
			for(final String[] row : this.rows)
			{
				target.accept(keyMapper.apply(row), valueMapper.apply(row));
			}

			return target;
		}


		static final class ColumnCountValidator implements Predicate<String[]>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final int columnCount;


			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			ColumnCountValidator(final int columnCount)
			{
				super();
				this.columnCount = notNegative(columnCount);
			}



			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final boolean test(final String[] row) throws ThrowBreak
			{
				return row.length != this.columnCount;
			}

		}

	}

}
