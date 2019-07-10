package one.microstream.test.corp.main;

import one.microstream.chars.VarString;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestStoragePerformanceSimple
{
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		final Object[] array = generateArray(1_000_000);
		STORAGE.setRoot(array);

		System.out.println("Storing array of " + array.length + " " + array[0].getClass().getSimpleName() + " ...");
		
		
		for(int i = 1; ;i++)
		{
			final long requiredTime = measureStore();
			System.out.println(
				VarString.New()
				.padLeft(Integer.toString(i), 7, '0').add(": ")
				.add(new java.text.DecimalFormat("00,000,000,000").format(requiredTime))
			);
			
			// must clear object registry to force storing of already persisted/known instances in the next run.
			STORAGE.persistenceManager().objectRegistry().clear();
		}
	}
	
	static long measureStore()
	{
		final long tStart = System.nanoTime();
		STORAGE.storeRoot();
		final long tStop = System.nanoTime();
		
		return tStop - tStart;
	}
	
	static Object[] generateArray(final int elementCount)
	{
		final Object[] array = new Object[elementCount];
		for(int i = 0; i < elementCount; i++)
		{
			// offset to avoid cached Integers
			array[i] = Integer.valueOf(255 + elementCount);
		}
		return array;
	}
	
}
