package net.jadoth.experimental.collections;

import net.jadoth.Jadoth;
import net.jadoth.concurrent.JadothThreads;
import net.jadoth.meta.JadothConsole;
import net.jadoth.util._longKeyValue;


public class MainTest_longConcMapThreaded
{
	static final int THREAD_COUNT = 100;
	static final int ADD_COUNT = 100;
	static final long[] KEYS = new long[THREAD_COUNT * ADD_COUNT];
	static int tid = 0;

	static final int PRINT_LIMIT = 100;

	public static void main(final String[] args)
	{
		final _longConcurrentMap map = _longConcurrentMap.New(THREAD_COUNT*ADD_COUNT, 1.0f);

		System.out.println("adding...");
		// multithreaded test
		for(int i = THREAD_COUNT; i --> 0;)
		{
			new Thread(){
				private final int offset = ADD_COUNT * tid++;
				@Override public void run(){
					for(int c = 0; c < ADD_COUNT; c++)
					{
						map.add(KEYS[this.offset + c] = Thread.currentThread().getId(), c);
					}
				}

			}.start();
		}
//		for(int i = 0; i < KEYS.length; i++)
//		{
//			System.out.println(i+"\t"+KEYS[i]);
//		}
		JadothThreads.sleep(ADD_COUNT*10);


		// singlethreaded test
//		for(int c = 0; c < ADD_COUNT; c++)
//		{
//			map.add(Long.toString(Thread.currentThread().getId()), "");
//		}


		System.out.println("add() check");
		_longKeyValue[] array;
		JadothConsole.printArray(array = map.toArray(), "{", ",", "}", PRINT_LIMIT);
		System.out.println(THREAD_COUNT*ADD_COUNT+" -> "+THREAD_COUNT + " == "+Jadoth.to_int(map.size())+" == "+actualArraySize(array));



		// removing //


		JadothThreads.sleep(ADD_COUNT*10);
		System.out.println("removing...");

		for(int i = THREAD_COUNT; i --> 0;)
		{
			new Thread(){
				private final int offset = ADD_COUNT * --tid;
				@Override public void run(){
					for(int c = 0; c < ADD_COUNT; c++)
					{
						map.remove(KEYS[this.offset + c]);
					}
				}

			}.start();
		}

		JadothThreads.sleep(ADD_COUNT*10);

		System.out.println("remove() check");

		_longKeyValue[] array2;
		JadothConsole.printArray(array2 = map.toArray(), "{", ",", "}", PRINT_LIMIT);
		System.out.println(THREAD_COUNT*ADD_COUNT+" -> "+THREAD_COUNT + " == "+Jadoth.to_int(map.size())+" == "+actualArraySize(array2));
	}

	static int actualArraySize(final Object[] array)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == null)
			{
				return i;
			}
		}
		return array.length;
	}

}
