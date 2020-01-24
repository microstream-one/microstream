package one.microstream.persistence.test;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.reference.Lazy;
import one.microstream.reference.LazyReferenceManager;
import one.microstream.reference.ObjectSwizzling;

public class MainTestLazyReferenceHandling
{
	static final ObjectSwizzling DUMMY_LOADER = o -> null;
	
	static XList<Lazy<String>> strings;

	public static void main(final String[] args) throws Exception
	{
		test(LazyReferenceManager.New(
			
			Lazy.CheckerMemory(0.5).combine(
				Lazy.CheckerTimeout(20_000)
			)
		));

		strings = createList(10_000);
			
//		new Thread(()->{
//		LazyReferenceManager.get().clear();
//		}).start();
		
		while(true)
		{
			Thread.sleep(XMath.random(300));
			final int count = XMath.random(5000);
			strings.addAll(createList(count));
			XDebug.println("Added " + count);
			System.gc();
		}
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
