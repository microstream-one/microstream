package net.jadoth.util.csv;


public interface CsvParser<I>
{
	public CsvConfiguration parseCsvData(
		CsvConfiguration              config                ,
		I                             input                 ,
		CsvSegmentsParser.Provider<I> segmentsParserProvider,
		CsvRowCollector               rowAggregator
	);

	public default CsvConfiguration parseCsvData(
		final CsvConfiguration              config                ,
		final I                             input                 ,
		final CsvRowCollector               rowAggregator
	)
	{
		return this.parseCsvData(config, input, null, rowAggregator);
	}


	public interface Creator<D>
	{
		public CsvParser<D> createCsvStringParser();
	}

	public interface Provider<D>
	{
		public CsvParser<D> provideCsvStringParser();

		public void disposeCsvStringParser(CsvParser<D> parser);
	}

}
