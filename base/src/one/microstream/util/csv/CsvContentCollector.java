package one.microstream.util.csv;

public interface CsvContentCollector extends CsvRowCollector
{
	public void completeTable(String name);
}
