package one.microstream.xcsv.internal;

import one.microstream.chars.StringTable;
import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.io.XIO;
import one.microstream.util.xcsv.XCsvContent;
import one.microstream.util.xcsv.XCsvContentBuilderCharArray;


public class MainTestXCsvPerformance
{
//	static final CsvConfiguration CONFIG = CSV.configurationBuilder()
//		.createConfiguration()
//	;
//
//	static final Procedure<String> columnNameCollector = new Procedure<String>() {
//		@Override
//		public void accept(final String e)
//		{
//			// no-op
//		}
//	};
//
//	static int rowCount = 0;
//
//	static final CsvRowCollector rowAggregator =
//		new CsvRowCollector()
//		{
//			private final Substituter<String> stringCache = Substituter.Default.<String>New();
//			private final BulkList<String>    row         = new BulkList<>();
//
//			@Override
//			public void accept(final String rowValue)
//			{
//				this.row.add(this.stringCache.substitute(rowValue));
//			}
//
//			@Override
//			public void completeRow()
//			{
//				this.row.clear();
//				rowCount++;
//			}
//
//			@Override
//			public void completeRows()
//			{
//				this.row.clear(); // cleanup
//			}
//		}
//	;



	public static void main(final String[] args) throws Throwable
	{
		for(int i = 100; i --> 0;)
		{
			final long tStart = System.nanoTime();
			doit();
			final long tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.gc();
		}
	}

	private static void doit() throws Throwable
	{
		final char[] input  = XIO.readString(
			XIO.Path("D:/TestExport.csv"),
			XChars.defaultJvmCharset()
		).toCharArray();

		final XCsvContentBuilderCharArray builder = XCsvContentBuilderCharArray.New();
		final XCsvContent                 parsed  = builder.build("data", _charArrayRange.New(input));
		final StringTable                table   = parsed.segments().first().value();

		System.out.println("Parsed row count = "+table.rows().size());
	}

}
