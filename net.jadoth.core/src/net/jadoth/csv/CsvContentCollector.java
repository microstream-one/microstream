package net.jadoth.csv;

public interface CsvContentCollector extends CsvRowCollector
{
	public void completeTable(String name);
}
