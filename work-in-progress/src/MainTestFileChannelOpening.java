import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.XIO;

public class MainTestFileChannelOpening
{
	/* (22.04.2020 TM)NOTE:
	 *  VERY rough estimate that only correlates loosely to measured data:
	 *  Opening a FileChannel takes ~52,000 ns
	 *  Reading file access takes ~6,000 ns
	 *  Reading 1 byte takes ~0.1 ns
	 * 
	 *  (on my ... rather old ... private PC's SSD)
	 */
	
	private static final int  RUNS   = 100;
	private static final int  LOOPS  = 1000;
	private static final int  BYTES  = 1000;
	private static final Path SOURCE = XIO.Path("C:/Files/bytes"+BYTES+".txt");
	private static final ByteBuffer DBB = ByteBuffer.allocateDirect(BYTES);
	private static final FileChannel FC = uncheckedOpenFileChannelReading(SOURCE);
	
	private static final FileChannel uncheckedOpenFileChannelReading(final Path source)
		throws IORuntimeException
	{
		try
		{
			return XIO.openFileChannelReading(source);
		}
		catch(final IOException e)
		{
			// FU
			throw new IORuntimeException(e);
		}
	}
	
	public static void main(final String[] args) throws IOException
	{
		
		long value;
		
		for(int i = RUNS; i --> 0;)
		{
			value = 0;
			final long tStart = System.nanoTime();
			test1();
			final long tStop = System.nanoTime();
			value += DBB.get(0);
			System.out.println(new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart) + " " + value);
		}
		
	}
	
	static final void test1() throws IOException
	{
		for(int i = LOOPS; i --> 0;)
		{
			try(final FileChannel fc = XIO.openFileChannelReading(SOURCE))
			{
				DBB.clear();
				XIO.read(fc, DBB);
			}
		}
	}
	
	static final void test2() throws IOException
	{
		for(int i = LOOPS; i --> 0;)
		{
			DBB.clear();
			XIO.read(FC, DBB);
		}
	}
	
}
