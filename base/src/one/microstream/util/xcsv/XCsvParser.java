package one.microstream.util.xcsv;


public interface XCsvParser<I>
{
	public XCsvConfiguration parseCsvData(
		XCsvConfiguration              config                ,
		I                             input                 ,
		XCsvSegmentsParser.Provider<I> segmentsParserProvider,
		XCsvRowCollector               rowAggregator
	);

	public default XCsvConfiguration parseCsvData(
		final XCsvConfiguration              config                ,
		final I                             input                 ,
		final XCsvRowCollector               rowAggregator
	)
	{
		return this.parseCsvData(config, input, null, rowAggregator);
	}


	public interface Creator<D>
	{
		public XCsvParser<D> createCsvStringParser();
	}

	public interface Provider<D>
	{
		public XCsvParser<D> provideCsvStringParser();

		public void disposeCsvStringParser(XCsvParser<D> parser);
	}

}
