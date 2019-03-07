package net.jadoth.persistence.test;

import java.util.Arrays;

import net.jadoth.collections.XArrays;
import net.jadoth.concurrency.XThreads;
import net.jadoth.math.XMath;

public class MainTestStackLocalConcurrency
{
	static char[] chars = new char[10];
	
	static final int THREAD_COUNT = 2;

	public static void main(final String[] args)
	{
		XArrays.fill(chars, '_');
		
		XThreads.start(MainTestStackLocalConcurrency::print);
		
		for(int r = 0; r < THREAD_COUNT; r++)
		{
			XThreads.start(MainTestStackLocalConcurrency::modify);
		}
	}
	
	static void print()
	{
		final char[] localChars = chars;
		final char[] buffer = new char[localChars.length];
		
		XThreads.sleep(1000);
		System.arraycopy(localChars, 0, buffer, 0, localChars.length);
		
		System.out.println("main: " + Arrays.toString(buffer));
	}
	
	static void modify()
	{
//		while(true)
		{
//			synchronized(chars)
			for(int i = 0; i < 100; i++)
			{
				XThreads.sleep(10);
				chars[XMath.random(chars.length)] = 'X';
				System.out.println(Arrays.toString(chars) + " (" + Thread.currentThread().getName()+")");
			}
		}
	}

}
