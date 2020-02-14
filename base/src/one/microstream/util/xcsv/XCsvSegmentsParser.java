package one.microstream.util.xcsv;


public interface XCsvSegmentsParser<I>
{
	public void parseSegments(I input);



	public interface Provider<I>
	{
		public XCsvSegmentsParser<I> provideSegmentsParser(XCsvConfiguration config, XCsvRowCollector rowAggregator);
	}
}
