/**
 *
 */


import one.microstream.concurrency.XThreads;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestSynchronized
{

	public void waitSynchronized()
	{
		synchronized(this) {
			for(int i = 0; i < 10; i++)
			{
				System.out.println(Thread.currentThread()+" waiting ("+i+")");
				XThreads.sleep(1000);
			}
		}
	}

	public void doOtherStuff()
	{
		// won't block whily waitSynchronized() is executed
		System.out.println(Thread.currentThread()+" does other stuff");

		// only this will block while waitSynchronized() is executed
//		synchronized(this) {
//			System.out.println(Thread.currentThread()+" does other stuff");
//		}
	}



	public static void main(final String[] args)
	{
		final MainTestSynchronized o = new MainTestSynchronized();


		for(int t = 0; t < 10; t++)
		{
			new Thread(){
				@Override
				public void run() {
					for(int i = 0; i < 10; i++)
					{
						o.doOtherStuff(); // will NOT be blocked by the 10-sec-long executing waitSynchronized()
						XThreads.sleep(1000);
					}
				}
			}.start();
		}
		o.waitSynchronized(); // is synchronized busy for 10 seconds
	}

}
