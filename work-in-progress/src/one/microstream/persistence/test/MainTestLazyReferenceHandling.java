package one.microstream.persistence.test;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.persistence.lazy.Lazy;
import one.microstream.persistence.lazy.LazyReferenceManager;
import one.microstream.persistence.types.PersistenceObjectRetriever;

public class MainTestLazyReferenceHandling
{
	static final PersistenceObjectRetriever DUMMY_LOADER = o -> null;
	
	static XList<Lazy<String>> strings;

	public static void main(final String[] args) throws Exception
	{
		test(LazyReferenceManager.New(
//			Lazy.Checker(
//				20_000,
//				memoryStats -> memoryStats.quota() < 0.5
//			),
			new LazyReferenceManager.Clearer(),
			() -> 2500,
			() -> 2_500_000
		));

		strings = createList(10_000);
			
//		new Thread(()->{
//		LazyReferenceManager.get().clear();
//		}).start();
		
//		while(true)
//		{
//			Thread.sleep(XMath.random(300));
//			final int count = XMath.random(1000);
//			strings.addAll(createList(count));
//			XDebug.println("Added " + count);
//			System.gc();
//		}
	}
	
	static XList<Lazy<String>> createList(final long size)
	{
		final BulkList<Lazy<String>> list = BulkList.New(size);
		for(long i = 0; i < size; i++)
		{
			list.add(Lazy.New("Test String " + i, i, DUMMY_LOADER));
		}
		return list;
	}

	static void test(final LazyReferenceManager lrm)
	{
		LazyReferenceManager.set(lrm.start());
//		LazyReferenceManager.set(null).stop();
	}
}
