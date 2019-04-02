package one.microstream.concurrent;

import java.util.function.Consumer;

import one.microstream.functional.ParallelProcedure;
import one.microstream.math.XMath;

public class MainTestParallelProcedure
{
	private static final ParallelProcedure.LogicProvider<String, Consumer<Object>> provider =
		new ParallelProcedure.LogicProvider.SingletonLogic<>(
			(final Object e) ->
			{
//				XThreads.sleep(10);
				System.out.println(Thread.currentThread()+":\t"+e);
//				XThreads.sleep(10);
//				if(e == null) throw new NullPointerException();
			}
		)
	;

	private static final ParallelProcedure<String> procedure =
		new ParallelProcedure.Implementation<>(provider, 10, 300)
	;

	public static void main(final String[] args) throws Exception
	{
		while(true)
		{
			for(int r = 10_000; r --> 0;)
			{
				procedure.accept(Integer.toString(r));
			}
			Thread.sleep(1000 + XMath.random(5000));
			System.gc();
//			break;
		}
//		Thread.sleep(1*60*1000);
	}

}
