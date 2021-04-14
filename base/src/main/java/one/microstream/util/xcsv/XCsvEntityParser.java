package one.microstream.util.xcsv;

import one.microstream.chars._charArrayRange;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XSequence;
import one.microstream.exceptions.XCsvException;
import one.microstream.typing.XTypes;

public interface XCsvEntityParser<T>
{

	public XGettingList<T> parse(final _charArrayRange input);

	public <C extends XSequence<? super T>> C parseInto(final _charArrayRange input, final C collector);



	public abstract class Abstract<T> implements XCsvEntityParser<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final int DEFAULT_COLLECTOR_CAPACITY = 1024;
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int columnCount             ;
		private final int collectorInitialCapacity;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(final int columnCount)
		{
			this(columnCount, DEFAULT_COLLECTOR_CAPACITY);
		}

		public Abstract(final int columnCount, final int collectorInitialCapacity)
		{
			super();
			this.columnCount = columnCount;
			this.collectorInitialCapacity = collectorInitialCapacity;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public BulkList<T> parse(final _charArrayRange input)
		{
			return this.parseInto(input, this.collector());
		}

		protected BulkList<T> collector()
		{
			return new BulkList<>(this.collectorInitialCapacity);
		}
		
		protected int columnCount()
		{
			return this.columnCount;
		}
		
		protected void beginTable(
			final String                   tableName  ,
			final XGettingSequence<String> columnNames,
			final XGettingList<String>     columnTypes
		)
		{
			// no-op
		}
		
		protected void completeTable()
		{
			// no-op
		}

		protected void validateRow(final BulkList<String> row)
		{
			if(XTypes.to_int(row.size()) == this.columnCount())
			{
				return;
			}
			throw new XCsvException("Column count mismatch (" + row.size() + " != " + this.columnCount() + ")");
		}



		protected abstract T apply(BulkList<String> row);
	}

}
