package net.jadoth.test.collections;

import static net.jadoth.concurrent.ThreadedInstantiating.threaded;

import java.util.ArrayList;

import net.jadoth.collections.BulkList;
import net.jadoth.concurrent.Threaded;
import net.jadoth.functional.Aggregator;
import net.jadoth.memory.Instantiator;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestBulkListEscapeAnalysis
{
	static final int SIZE = 1000*1000*10;
	static final Integer ints[] = new Integer[SIZE];
	static final BulkList<Integer> gl = new BulkList<>(SIZE);
	static final ArrayList<Integer> al = new ArrayList<>(SIZE);
	static {
		for(int i = 0; i < SIZE; i++)
		{
			gl.add(i);
			al.add(i);
		}
	}
	static final int RUNS = 20;


	static final Aggregator<Integer, Integer> SUM = new Aggregator<Integer, Integer>() {
		private int sum = 0;
		@Override public void accept(final Integer element) {
			this.sum += element.intValue();
		}
		@Override public Integer yield() { return this.sum;	}
	};

	private static final Threaded<Aggregator<Integer, Integer>> LOCAL_SUM = threaded(
		new Instantiator<Aggregator<Integer, Integer>>() { @Override	public Aggregator<Integer, Integer> newInstance() {
			return new Aggregator<Integer, Integer>(){
				private int sum = 0;
				@Override public void accept(final Integer element) {
					this.sum += element.intValue();
				}
				@Override public Integer yield() { return this.sum;	}
			};
		}}
	);

	public static void main(final String[] args)
	{
		final BulkList<Integer> gl = MainTestBulkListEscapeAnalysis.gl;
		final ArrayList<Integer> al = MainTestBulkListEscapeAnalysis.al;
		long tStart, tStop;

		int sum = 0;
		LOCAL_SUM.get();

		for(int k = 0; k < RUNS; k++)
		{
			sum = 0;
			tStart = System.nanoTime();

//			sum = gl.iterate(LOCAL_SUM.get().reset());
//			sum = gl.iterate(SUM.reset());
			sum = gl.iterate(new Aggregator<Integer, Integer>() {
				private int sum = 0;
				@Override public void accept(final Integer element) { this.sum += element.intValue(); }
				@Override public Integer yield() { return this.sum;	}
			}).yield();
			tStop = System.nanoTime();
			System.out.print(sum);
			System.out.println(" GL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

		System.out.println();

		for(int k = 0; k < RUNS; k++)
		{
			sum = 0;
			tStart = System.nanoTime();
			for(final Integer i : al)
			{
				sum += i;
			}
			tStop = System.nanoTime();
			System.out.print(sum);
			System.out.println(" AL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

		System.out.println();

		for(int k = 0; k < 5; k++)
		{
			sum = 0;
			tStart = System.nanoTime();
			for(int i = 0; i < SIZE; i++)
			{
				sum += i;
			}
			tStop = System.nanoTime();
			System.out.print(sum);
			System.out.println(" DY Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

	}

}
