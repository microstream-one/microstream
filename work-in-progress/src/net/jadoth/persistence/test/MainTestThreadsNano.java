package net.jadoth.persistence.test;

public class MainTestThreadsNano
{
	public static void main(final String[] args) throws Throwable
	{
		final Ticker[] tickers = new Ticker[4];
		for(int i = 0; i < tickers.length; i++)
		{
			(tickers[i] = new Ticker()).start();
		}

		while(true)
		{
			for(int i = 0; i < tickers.length; i++)
			{
				System.out.println(tickers[i].getId()+"\t"+tickers[i].nanotime);
			}
			System.out.println("---");
			Thread.sleep(100);
		}
	}

	static final class Ticker extends Thread
	{
		volatile long nanotime;

		@Override
		public void run()
		{
			while(true)
			{
				this.nanotime = System.nanoTime();
			}
		}
	}

}
