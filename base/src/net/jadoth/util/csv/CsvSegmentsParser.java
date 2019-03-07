package net.jadoth.util.csv;


public interface CsvSegmentsParser<I>
{
	public void parseSegments(I input);



	public interface Provider<I>
	{
		public CsvSegmentsParser<I> provideSegmentsParser(CsvConfiguration config, CsvRowCollector rowAggregator);
	}
}
