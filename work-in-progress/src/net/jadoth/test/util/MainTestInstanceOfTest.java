/**
 * 
 */
package net.jadoth.test.util;



/**
 * @author Thomas Muenz
 *
 */
public class MainTestInstanceOfTest
{
	static final int COUNT = 1024;
	static final Object[] strings = new Object[COUNT];
	
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
		long tStart, tStop;
			
		//won't work :(
//		final ThreadMXBean mxb = ManagementFactory.getThreadMXBean();		
//		final com.sun.management.OperatingSystemMXBean omxb = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
	
		int x = 0;
		
		for(int i = 10; i -->0;)
		{
			x = 0;
			tStart = System.nanoTime();
			for(Object o : strings)
			{
				if(o instanceof String)
				{
//				if(o == null)
//				{
					x++;
				}
			}
			tStop = System.nanoTime();
			System.out.println(x);
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		
		for(int i = 10; i -->0;)
		{
			x = 0;
			tStart = System.nanoTime();
			for(Object o : strings)
			{
				if(o.getClass() == String.class)
				{
					x++;
				}
			}
			tStop = System.nanoTime();
			System.out.println(x);
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		
		
		


	}

}
