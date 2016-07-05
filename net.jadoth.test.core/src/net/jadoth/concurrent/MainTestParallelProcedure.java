package net.jadoth.concurrent;

import java.util.function.Consumer;

import net.jadoth.collections.functions.ParallelProcedure;
import net.jadoth.math.JadothMath;

public class MainTestParallelProcedure
{
	private static final ParallelProcedure.LogicProvider<String, Consumer<Object>> provider =
		new ParallelProcedure.LogicProvider.SingletonLogic<>(
//			Jadoth.System_out_println
			new Consumer<Object>() {
				@Override public void accept(final Object e) {
//					JaThreads.sleep(10);
					System.out.println(Thread.currentThread()+":\t"+e);
//					JaThreads.sleep(10);
//					if(e == null) throw new NullPointerException();
				}
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
			Thread.sleep(1000 + JadothMath.random(5000));
			System.gc();
//			break;
		}
//		Thread.sleep(1*60*1000);
	}

}
