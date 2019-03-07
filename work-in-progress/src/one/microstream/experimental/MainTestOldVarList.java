package one.microstream.experimental;

import static one.microstream.math.XMath.sequence;

import java.util.Arrays;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.math.XMath;
import one.microstream.typing.XTypes;

public class MainTestOldVarList
{
	static final int RUNS = 100;
	static final int SIZE = 1000*1000;
	static final Integer[] INTS = XMath.sequence((Integer)(SIZE-1));
//	static final int[] INTS = JaMath.sequence(SIZE-1);


	static void test(final OldVarList<Integer> ints, final int index)
	{
		System.out.println(index+" = "+ints.get(index));
	}

	public static void main(final String[] args)
	{
		final OldVarList<Integer> ints = new OldVarList<Integer>().addAll(INTS);
//		final BulkList<Integer> ints = new BulkList<Integer>().addAll(INTS);

//		test(ints, 0);
//		test(ints, 1);
//		test(ints, 2);
//		test(ints, 20);
//		test(ints, 200);
//		test(ints, SIZE/2 - 1);


		for(int r = RUNS; r --> 0;)
		{
			int last = 0;
			final long tStart = System.nanoTime();
			for(int i = SIZE/2; i < SIZE; i++)
			{
				last = ints.get(i);
			}
			final long tStop = System.nanoTime();
			System.out.println(last+" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}


//		testAddSingles();
//		testAddArrays();
//		testInsertSingles();
//		testIterationPerformance();
//		testIndex();


//		for(int k = 10000; k --> 0;)
//		{
//			final LinkedList<Integer> segs = new LinkedList<Integer>();
//			long tStart, tStop;
//			tStart = System.nanoTime();
//			for(int i = 0; i < SIZE; i++)
//			{
////				segs.add(INTS[i]);
//				segs.add(0,INTS[i]);
//			}
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}

//		for(int k = 10000; k --> 0;)
//		{
//			final ArrayList<Integer> segs = new ArrayList<Integer>(SIZE);
//			long tStart, tStop;
//			tStart = System.nanoTime();
//			for(int i = 0; i < SIZE; i++)
//			{
//				segs.add(INTS[i]);
////				segs.insert(i>>1,INTS[i]);
//			}
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}

//		for(int k = 10000; k --> 0;)
//		{
//			final VarList<Integer> segs = new VarList<Integer>().setSegmentSize(1024);//.disableIndex();
//			long tStart, tStop;
//			tStart = System.nanoTime();
//			for(int i = 0; i < SIZE; i++)
//			{
//				segs.add2(INTS[i]);
////				segs.insert(0,INTS[i]);
//			}
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}

//		for(int k = 10000; k --> 0;)
//		{
//			final BulkList<Integer> segs = new BulkList<Integer>(1024*1024);
//			long tStart, tStop;
//			tStart = System.nanoTime();
//			for(int i = 0; i < SIZE; i++)
//			{
//				segs.add(INTS[i]);
////				segs.insert(i>>1,INTS[i]);
//			}
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}

//		for(int k = 10000; k --> 0;)
//		{
//			final LimitList<Integer> segs = new LimitList<Integer>(SIZE);
//			long tStart, tStop;
//			tStart = System.nanoTime();
//			for(int i = 0; i < SIZE; i++)
//			{
//				segs.add(INTS[i]);
////				segs.insert(i>>1,INTS[i]);
//			}
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}


	}


	static void testIterationPerformance()
	{
//		final XReference<Integer> r = ref(null);
		final int[] ri = X.ints(0);
		final BulkList<Integer> segs = new BulkList<Integer>().addAll(INTS);
		for(int k = 10000; k --> 0;)
		{
			long tStart, tStop;
			tStart = System.nanoTime();
			segs.iterate(new Consumer<Integer>() {
				@Override public void accept(final Integer e) {
//					r.set(e);
					ri[0]++;
				}
			});
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			System.out.println(r.get());
			System.out.println(ri[0]);
		}

	}


	static void testIndex()
	{
		final OldVarList<Integer> segs = new OldVarList<>();
		for(int i = 0; i < SIZE; i++)
		{
			segs.add2(INTS[i]);
		}
		checkGet(segs, 0);
		checkGet(segs, 1);
		checkGet(segs, 2);
		checkGet(segs, 8);
		checkGet(segs, 9);
		checkGet(segs,10);
		checkGet(segs,11);
		checkGet(segs,20);
		checkGet(segs,21);
		checkGet(segs,23);
		checkGet(segs,24);
		checkGet(segs,25);
		checkGet(segs,32);
		checkGet(segs,33);
		checkGet(segs,SIZE-2);
		checkGet(segs,SIZE-1);
	}

	static void checkGet(final OldVarList<Integer> segs, final int index)
	{
		System.out.println(index+":\t"+segs.get2(index));
	}


	static void testAddSingles()
	{
		final OldVarList<Integer> segs = new OldVarList<>();
		final BulkList<Integer> bulk = new BulkList<>();
		for(int i = 0; i < 1000; i++)
		{
			bulk.add(INTS[i]);
			segs.add(INTS[i]);
		}
		check(segs, bulk);
	}

	static void testInsertSingles()
	{
		final OldVarList<Integer> segs = new OldVarList<Integer>().addAll(0, 0, 0);
		final BulkList<Integer> bulk = new BulkList<Integer>().addAll(0, 0, 0);
		for(int i = 0; i < 30; i++)
		{
			bulk.insert(1, INTS[i]);
			segs.insert(1, INTS[i]);
		}
		check(segs, bulk);
	}

	static void testAddArrays()
	{
		final OldVarList<Integer> segs = new OldVarList<>();
		final BulkList<Integer> bulk = new BulkList<>();
		for(int i = 0; i < 100; i++)
		{
			bulk.addAll(sequence((Integer)i));
			segs.addAll(sequence((Integer)i));
		}
		check(segs, bulk);
		checkEach("testAddArrays each", segs, bulk);
	}


	static void checkEach(final String name, final OldVarList<Integer> segs, final BulkList<Integer> bulk)
	{
		if(XTypes.to_int(bulk.size()) != segs.size)
		{
			System.out.println(false+"\t"+name);
			return;
		}
		for(int i = 0, size = XTypes.to_int(bulk.size()); i < size; i++)
		{
			if(!bulk.at(i).equals(segs.get(i)))
			{
				System.out.println(false+"\t"+name);
				return;
			}
		}
		System.out.println(true+"\t"+name);
	}


	static void check(final String name, final OldVarList<Integer> segs, final BulkList<Integer> bulk)
	{
		System.out.println(Arrays.equals(bulk.toArray(), segs.toArray())+"\t"+name);
	}
	static void check(final OldVarList<Integer> segs, final BulkList<Integer> bulk)
	{
		check(new Throwable().getStackTrace()[1].getMethodName(), segs, bulk);
	}
}

