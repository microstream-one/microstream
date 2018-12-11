package net.jadoth.experimental;

import net.jadoth.collections.VarList;
import net.jadoth.low.XMemory;
import net.jadoth.typing.XTypes;

public class MainTestVarListPerformance
{
	public static void main(final String[] args)
	{
		System.out.println(XMemory.byteSizeReference());

		final int size = 1_000_000;

		while(true)
		{
			final VarList<String>  list = VarList.New();
//			final BulkList<String> list = BulkList.New();
//			final VarList<String>  list = VarList.New(size);
//			final BulkList<String> list = BulkList.New(size);
//			final LimitList<String> list = LimitList.New(size);
			System.out.println("Starting run");
			System.out.flush();
			final long tStart = System.nanoTime();
			for(int s = 0; s < size; s++)
			{
				list.add(null);
			}
			final long tStop = System.nanoTime();
			System.out.println(XTypes.to_int(list.size())+" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.out.flush();
			System.gc();
			final VarList<String>  list2 = VarList.New();
			for(int s = 0; s < size; s++)
			{
				list2.add(null);
			}
			System.out.println(XTypes.to_int(list2.size()));
			System.gc();
		}
	}
}
