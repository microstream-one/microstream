package net.jadoth.test.util;

import static net.jadoth.concurrent.JadothThreads.sleep;
import static net.jadoth.concurrent.JadothThreads.start;
import static net.jadoth.concurrent.ThreadedInstantiating.threaded;
import static net.jadoth.math.JadothMath.random;
import net.jadoth.collections.LimitList;
import net.jadoth.concurrent.Threaded;
import net.jadoth.memory.Instantiator;
import net.jadoth.util.chars.VarString;

/**
 * This Test shows that although multiple Threads use the same shared static variable, each thread gets his own private
 * {@link VarString} instance managed by the {@link Threaded}.
 * <p>
 * The conceptual / architectural pupose of the {@link Threaded} class is that the state in implementations
 * can be made implicitely thread-safe without caring too much about synchronizing.
 * <p>
 * In other words:<br>
 * The context (state) of a thread can be located inside a class instead of inside the Thread itself.
 *
 *
 * @author Thomas Muenz
 *
 */
public class MainTestThreadLocal
{
	static final Instantiator<VarString> VARCHAR_FACTORY = new Instantiator<VarString>(){
		@Override public VarString newInstance(){
			return VarString.New();
		}
	};

	// single static ("global") instance that is used by all threads, yet with thread-safe / thread-private content
	static final Threaded<VarString> THREAD_VARCHAR = threaded(VARCHAR_FACTORY);

	// with project lambda salvation
//	static final ThreadContext<VarString> THREAD_VARCHAR = threaded(#{VarString.New()});



	static final int STRINGS_SIZE = 100;
	static final int THREAD_COUNT = 1000;
	static final LimitList<String> STRINGS = new LimitList<>(STRINGS_SIZE);
	static {
		for(int i = 0; i < STRINGS_SIZE; i++)
		{
			STRINGS.add(Integer.toString(i));
		}
	}

	static final class AggregatorThread implements Runnable
	{
		private void appendId()
		{
			THREAD_VARCHAR.get().append('(').add(Thread.currentThread().getId()).append(')');
		}

		@Override public void run()
		{
			sleep(random(1000));

			for(final String s : STRINGS)
			{
				// intentionally use inefficient subsequent call of get() to provoke many thread context lookups
				THREAD_VARCHAR.get().add(s);
				this.appendId();
				THREAD_VARCHAR.get().append('\t');
			}
			System.out.println("Thread "+Thread.currentThread().getId()+ "\tyields: "+THREAD_VARCHAR.get());
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		System.out.println("Read: \nThread(id)\tyields: #(id) ...\n---");
		// create and start all threads
		for(int t = 0; t < THREAD_COUNT; t++)
		{
			sleep(10);
			start(new AggregatorThread());
		}
		System.out.println("done");
		System.gc();
		THREAD_VARCHAR.consolidate();
		System.out.println("gced");

		start(new AggregatorThread());

	}

}



