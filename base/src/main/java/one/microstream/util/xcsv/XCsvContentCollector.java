package one.microstream.util.xcsv;

public interface XCsvContentCollector extends XCsvRowCollector
{
	public void completeTable(String name);
}
