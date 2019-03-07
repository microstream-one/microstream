package one.microstream.experimental.collections;

import one.microstream.concurrency.XThreads;
import one.microstream.meta.XDebug;
import one.microstream.typing.KeyValue;


public class MainTestConcMapThreaded
{
	static final int THREAD_COUNT = 100;
	static final int ADD_COUNT = 100;
	static final String[] KEYS = new String[THREAD_COUNT * ADD_COUNT];
	static int tid = 0;

	static final int PRINT_LIMIT = 100;

	public static void main(final String[] args)
	{
		final ExperimentalLockFreeConcurrentHashMap<String, String> map = new ExperimentalLockFreeConcurrentHashMap<>();
//		final ExperimentalLockFreeConcurrentHashMap<String, String> map = ExperimentalLockFreeConcurrentHashMap.create(KEYS.length, 1.0f);

		System.out.println("adding...");
		// multithreaded test
		for(int i = THREAD_COUNT; i --> 0;)
		{
			new Thread(){
				private final int offset = ADD_COUNT * tid++;
				@Override public void run(){
					for(int c = 0; c < ADD_COUNT; c++)
					{
						map.add(KEYS[this.offset + c] = Long.toString(Thread.currentThread().getId()), "");
					}
				}

			}.start();
		}
//		for(int i = 0; i < KEYS.length; i++)
//		{
//			System.out.println(i+"\t"+KEYS[i]);
//		}
		XThreads.sleep(ADD_COUNT*10);


		// singlethreaded test
//		for(int c = 0; c < ADD_COUNT; c++)
//		{
//			map.add(Long.toString(Thread.currentThread().getId()), "");
//		}


		System.out.println("add() check");
		KeyValue<String, String>[] array;
		XDebug.printArray(array = map.toArray(), "{", ",", "}", PRINT_LIMIT);
		System.out.println(THREAD_COUNT*ADD_COUNT+" -> "+THREAD_COUNT + " == "+map.size()+" == "+actualArraySize(array));



		// removing //


		XThreads.sleep(ADD_COUNT*10);
		System.out.println("removing...");

		for(int i = THREAD_COUNT; i --> 0;)
		{
			new Thread(){
				private final int offset = ADD_COUNT * --tid;
				@Override
				public void run(){
					for(int c = 0; c < ADD_COUNT; c++)
					{
						map.remove(KEYS[this.offset + c]);
					}
				}

			}.start();
		}

		XThreads.sleep(ADD_COUNT*10);

		System.out.println("remove() check");

		KeyValue<String, String>[] array2;
		XDebug.printArray(array2 = map.toArray(), "{", ",", "}", PRINT_LIMIT);
		System.out.println(THREAD_COUNT*ADD_COUNT+" -> "+THREAD_COUNT + " == "+map.size()+" == "+actualArraySize(array2));
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
