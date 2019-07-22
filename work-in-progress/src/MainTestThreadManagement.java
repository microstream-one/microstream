import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


public class MainTestThreadManagement
{
	public static void main(final String[] args)
	{
		final ThreadMXBean mx = ManagementFactory.getThreadMXBean();
		final java.text.DecimalFormat df = new java.text.DecimalFormat("00,000,000,000");

		long currCpuTime, lastCpuTime = mx.getCurrentThreadCpuTime();
		long currUsrTime, lastUsrTime = mx.getCurrentThreadUserTime();
		long currSysTime, lastSysTime = System.nanoTime();

		int i = 0;
		while(true)
		{
			i++;
			if(i % 1000000000 == 0)
			{
				currCpuTime = mx.getCurrentThreadCpuTime();
				currUsrTime = mx.getCurrentThreadUserTime();
				currSysTime = System.nanoTime();
				System.out.println("SYS Time: "+df.format(currSysTime - lastSysTime));
				System.out.println("CPU Time: "+df.format(currCpuTime - lastCpuTime));
				System.out.println("USR Time: "+df.format(currUsrTime - lastUsrTime));
				System.out.println();
				lastCpuTime = currCpuTime;
				lastUsrTime = currUsrTime;
				lastSysTime = currSysTime;
			}
		}
	}
}
