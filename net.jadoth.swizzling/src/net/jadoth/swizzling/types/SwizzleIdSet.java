package net.jadoth.swizzling.types;

import net.jadoth.collections.CapacityExceededException;
import net.jadoth.collections.interfaces.Sized;
import net.jadoth.collections.interfaces._longCollector;
import net.jadoth.functional._longIterable;
import net.jadoth.functional._longProcedure;

public interface SwizzleIdSet extends _longIterable, Sized
{
	@Override
	public long size();

	@Override
	public void iterate(_longProcedure iterator);



	final class Implementation implements SwizzleIdSet, _longCollector
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int DEFAULT_CAPACITY = 64;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long[] data = new long[DEFAULT_CAPACITY];
		private int    size;



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final long id)
		{
			if(this.size >= this.data.length)
			{
				if(this.size >= Integer.MAX_VALUE)
				{
					throw new CapacityExceededException();
				}
				System.arraycopy(this.data, 0, this.data = new long[(int)(this.data.length * 2.0f)], 0, this.size);
			}
			this.data[this.size++] = id;
		}

		@Override
		public void iterate(final _longProcedure procedure)
		{
			final long[] data = this.data;
			final int    size = this.size;

			for(int i = 0; i < size; i++)
			{
				procedure.accept(data[i]);
			}
		}

		@Override
		public long size()
		{
			return this.size;
		}

		@Override
		public boolean isEmpty()
		{
			return this.size == 0;
		}

	}

}
