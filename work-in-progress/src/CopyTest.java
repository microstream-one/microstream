import net.jadoth.math.XMath;


public class CopyTest
{
	private static int size = 1024;
	private static byte[] in = new byte[size];
	private static byte[] out = new byte[size];
	
	static int len = 0;

	public static void main(final String[] args) throws Exception
	{		
		for(int i = 0; i < size; i++)
		{
			in[i] = (byte)XMath.random(127);
		}
		
		final byte[] in = CopyTest.in;
		final byte[] out = CopyTest.out;
		
		len = in.length;
		
		long tStart;
		long tStop;
		
		
		Object[] bla;
		
		System.out.println("Array instantiation");
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		bla = new Object[size];
		tStop = System.nanoTime();
//		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		bla = new Object[size];
		tStop = System.nanoTime();
		System.out.println(bla.length);
		printTime(tStop - tStart);
		
		
//		if(true) return;

		System.out.println("System.arraycopy");
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		
		
		final int l = len;		
		System.out.println("(manual)");
		
		
		tStart = System.nanoTime();
		System.arraycopy(in, 0, out, 0, size);
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < in.length; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		printTime(tStop - tStart);
		
		tStart = System.nanoTime();
		for(int j = 0; j < l; j++)
		{
			out[j] = in[j];
		}
		tStop = System.nanoTime();
		
		
	}
	
	
	static void printTime(final long time)
	{
		//550 is the measured time it takes to mease the time (o_0)
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(time - 550));
	}
	
}
