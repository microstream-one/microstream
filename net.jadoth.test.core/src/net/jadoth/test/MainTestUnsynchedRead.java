package net.jadoth.test;

import java.util.ArrayList;

import net.jadoth.concurrency.XThreads;

public class MainTestUnsynchedRead
{
	static final ArrayList<String> strings = new ArrayList<>();


	static void print()
	{
		System.out.println(strings.size());
	}

	static void increase(final int t)
	{
		synchronized(strings) {
			strings.add(Long.toString(System.currentTimeMillis()));
			System.out.println(t+" added, size = "+strings.size());
		}
	}

	public static void main(final String[] args)
	{
		new Thread(){
			@Override
			public void run() {
				while(true)
				{
					print();
					XThreads.sleep(100);
				}
			}
		}.start();


		XThreads.sleep(1000);


		for(int t = 10; t --> 0;)
		{
			final int t1 = t;
			new Thread(){ @Override public void run() {
				while(true)
				{
					increase(t1);
					XThreads.sleep(10);
				}
				}
			}.start();
		}
	}
}
