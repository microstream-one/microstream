package one.microstream.persistence.test;

import one.microstream.X;
import one.microstream.collections.types.XList;
import one.microstream.persistence.lazy.Lazy;
import one.microstream.persistence.lazy.LazyReferenceManager;
import one.microstream.persistence.types.PersistenceObjectRetriever;

public class MainTestLazyReferenceHandling
{
	static final PersistenceObjectRetriever DUMMY_LOADER = o -> null;


	public static void main(final String[] args) throws Exception
	{
		test(LazyReferenceManager.New(10_000));

		final XList<Lazy<String>> strings = X.List(
			Lazy.New("A", 1, DUMMY_LOADER),
			Lazy.New("B", 2, DUMMY_LOADER),
			Lazy.New("C", 3, DUMMY_LOADER),
			Lazy.New("D", 4, DUMMY_LOADER),
			Lazy.New("E", 5, DUMMY_LOADER),
			Lazy.New("F", 6, DUMMY_LOADER),
			Lazy.New("G", 7, DUMMY_LOADER),
			Lazy.New("H", 8, DUMMY_LOADER)
		);

		System.out.println(strings);


		for(int i = 10; i --> 0;)
		{
			Thread.sleep(10_000);
			System.gc();
		}

	}

	static void test(final LazyReferenceManager lrm)
	{
		LazyReferenceManager.set(lrm.start());
//		LazyReferenceManager.set(null).stop();
	}
}
