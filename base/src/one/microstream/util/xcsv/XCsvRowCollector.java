package one.microstream.util.xcsv;

import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.functional._charRangeProcedure;


public interface XCsvRowCollector extends _charRangeProcedure
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
