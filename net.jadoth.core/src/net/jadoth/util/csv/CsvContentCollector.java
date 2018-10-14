package net.jadoth.util.csv;

public interface CsvContentCollector extends CsvRowCollector
{
	public void completeTable(String name);
}
