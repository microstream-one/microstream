package one.microstream.util.csv;

import one.microstream.chars.VarString;
import one.microstream.collections.ConstList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.typing.XTypes;

public interface CsvEntityAssembler<T> extends CsvRowAssembler<T>
{
	public XImmutableSequence<String> columnHeader();

	public VarString createCollector(final int entityCount);

	public default VarString assemble(final XGettingCollection<T> entities)
	{
		return this.assembleInto(this.createCollector(XTypes.to_int(entities.size())), this.columnHeader(), entities);
	}

	public VarString assembleInto(
		VarString                vs          ,
		XGettingSequence<String> columnHeader,
		XGettingCollection<T>    entities
	);

	public default VarString assembleInto(final VarString vs, final XGettingCollection<T> entities)
	{
		this.assembleInto(vs, this.columnHeader(), entities);
		return vs;
	}

	public abstract class AbstractImplementation<T> implements CsvEntityAssembler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		private static final int DEFAULT_ROW_COUNT_ESTIAMTE = 100;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XImmutableSequence<String> columnHeader        ;
		private final int                        rowCharCountEstimate;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final XGettingSequence<String> columnHeader)
		{
			this(columnHeader, DEFAULT_ROW_COUNT_ESTIAMTE);
		}

		protected AbstractImplementation(final XGettingSequence<String> columnHeader, final int rowCharCountEstimate)
		{
			super();
			this.columnHeader         = columnHeader.immure();
			this.rowCharCountEstimate = rowCharCountEstimate ;
		}

		protected AbstractImplementation(final String... columnHeader)
		{
			this(ConstList.New(columnHeader));
		}

		protected AbstractImplementation(final int rowCharCountEstimate, final String... columnHeader)
		{
			this(ConstList.New(columnHeader), rowCharCountEstimate);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public final XImmutableSequence<String> columnHeader()
		{
			return this.columnHeader;
		}

		@Override
		public VarString createCollector(final int entityCount)
		{
			return VarString.New(entityCount * this.rowCharCountEstimate);
		}

		@Override
		public abstract VarString assembleInto(
			VarString                vs          ,
			XGettingSequence<String> columnHeader,
			XGettingCollection<T>    entities
		);

	}

}
