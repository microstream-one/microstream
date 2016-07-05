package net.jadoth.csv;

import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._charRangeProcedure;


public interface CsvRowCollector extends _charRangeProcedure
{
	public void beginTable(
		String                   tableName  ,
		XGettingSequence<String> columnNames,
		XGettingList<String>     columnTypes
	);
	
	@Override
	public void accept(char[] data, int offset, int length);
	
	/**
	 * Calls without collected values (e.g. repeated calls) may not have undesired effects.
	 */
	public void completeRow();

	/**
	 * Calls without collected rows (e.g. repeated calls) may not have undesired effects.
	 */
	public void completeTable();
	
}
