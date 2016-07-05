package net.jadoth.util.chars;

import net.jadoth.Jadoth;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XSequence;

public interface CsvEntityParser<T>
{

	public XGettingList<T> parse(final _charArrayRange input);

	public <C extends XSequence<? super T>> C parseInto(final _charArrayRange input, final C collector);



	public abstract class AbstractImplementation<T> implements CsvEntityParser<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		private static final int DEFAULT_COLLECTOR_CAPACITY = 1024;
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int columnCount             ;
		private final int collectorInitialCapacity;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractImplementation(final int columnCount)
		{
			this(columnCount, DEFAULT_COLLECTOR_CAPACITY);
		}

		public AbstractImplementation(final int columnCount, final int collectorInitialCapacity)
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

		protected void validateRow(final BulkList<String> row)
		{
			if(Jadoth.to_int(row.size()) == this.columnCount)
			{
				return;
			}
			// (18.04.2016)EXCP: proper exception
			throw new RuntimeException("Column count mismatch (" + row.size() + " != " + this.columnCount + ")");
		}



		protected abstract T apply(BulkList<String> row);
	}

}
