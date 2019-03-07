import one.microstream.util.pooling.Pool;
import one.microstream.util.pooling.ThreadedPool;

public class MainTestThreadedPool
{
	static int value = 0;

	static String createString()
	{
		final String s = "String"+Integer.valueOf(++value).toString();
		System.out.println("creating "+s);
		return s;
	}

	static void dispatchString(final String s)
	{
		System.out.println("dispatching "+s);
	}

	static void returnString(final String s)
	{
		System.out.println("returning "+s);
	}

	static void closeString(final String s)
	{
		System.out.println("closing "+s);
	}

	static int provideWaitTimeout()
	{
		System.out.println("Begin waiting for new element.");
		return 1000;
	}

	static int provideWaitInterval()
	{
		System.out.println("Waiting for new element...");
		return 100;
	}

	static int calculateCloseCount(
		final int  maxConnectionCount  ,
		final int  totalConnectionCount,
		final int  freeConnectionCount ,
		final long lastGetTime         ,
		final long lastReturnTime      ,
		final long lastCloseTime
	)
	{
		final int closeCount = System.currentTimeMillis() - lastGetTime > 100000 ?1 :0;
		if(closeCount > 0)
		{
			System.out.println("pool closing "+closeCount);
		}
		return closeCount;
	}

	static RuntimeException createException(final String message, final Throwable cause)
	{
		return new RuntimeException(message, cause);
	}


	public static void main(final String[] args)
	{
		final Pool.Configuration<String> config = new Pool.Configuration.Implementation<>(
			10,
			1000,
			MainTestThreadedPool::createString,
			MainTestThreadedPool::dispatchString,
			MainTestThreadedPool::returnString,
			MainTestThreadedPool::closeString,
			MainTestThreadedPool::provideWaitTimeout,
			MainTestThreadedPool::provideWaitInterval,
			MainTestThreadedPool::calculateCloseCount,
			MainTestThreadedPool::createException
		);

		final ThreadedPool<String> stringPool = new ThreadedPool<>(config);

		// tries to get one element from the pool in each loop without ever giving it back (runs the pool dry)
		for(int i = 0; i < 20; i++)
		{
			final String s = stringPool.get();
			System.out.println("run "+(i+1)+": "+s);
//			stringPool.takeBack(s);
		}

		System.exit(0);

	}
}
