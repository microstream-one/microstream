/**
 *
 */
package net.jadoth.test.util;

import java.util.ArrayList;

import net.jadoth.collections.BulkList;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestFastListPerformance
{
	static final int COUNT = 1024+1;
	static final String[] strings = new String[COUNT];

	static {
		for(int i = 0, count = COUNT; i < count; i++)
		{
			strings[i] = Integer.toString(i);
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final ArrayList<String> arrayList = new ArrayList<>();
		final BulkList<String> jaArrayList = new BulkList<>(1024*1024);
		final BulkList<String> jaArrayList2 = new BulkList<>();
		System.out.println(jaArrayList2);



		long tStart, tStop;

		//won't work :(
//		final ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
//		final com.sun.management.OperatingSystemMXBean omxb = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();


		for(int i = 10; i -->0;)
		{
			tStart = System.nanoTime();
			for(final String s : strings)
			{
				arrayList.add(s);
			}
			tStop = System.nanoTime();
			arrayList.clear();
			System.out.println("Elapsed Time (AL): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));



			tStart = System.nanoTime();
			for(final String s : strings)
			{
				jaArrayList.add(s);
			}
			tStop = System.nanoTime();
			jaArrayList.clear();
			System.out.println("Elapsed Time (FL): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		}


//		for(int i = 10; i -->0;)
//		{
//			for(final String s : strings)
//			{
//				jaArrayList.add(s);
//			}
//
//			tStart = System.nanoTime();
//			jaArrayList.clear();
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time (JAL): " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}




	}

}
