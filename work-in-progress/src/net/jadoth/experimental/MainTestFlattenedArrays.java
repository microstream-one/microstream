/**
 * 
 */
package net.jadoth.experimental;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestFlattenedArrays
{
	private static final int DIM_X = 256;
	private static final int DIM_Y = 256;

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final int[][] dim2 = new int[DIM_X][DIM_Y];		
		final int[] dim1 = new int[DIM_X*DIM_Y];
		int result = 0;
		
//		for(final int[] is : dim2)
//		{
//			for(final int i : is)
//			{
//				result += i;
//			}
//		}
		
//		for(int x = 0; x < DIM_X; x++)
//		{
//			for(int y = 0; y < DIM_Y; y++)
//			{
//				result += dim2[x][y];
//			}
//		}
//		
//		for(int x = 0; x < DIM_X; x++)
//		{
//			for(int y = 0; y < DIM_Y; y++)
//			{
//				result += dim1[x + DIM_Y*y];
//			}
//		}
		
		
		long tStart, tStop;
		
		
//		result = 0;
//		
//		tStart = System.nanoTime();
//		for(final int[] is : dim2)
//		{
//			for(final int i : is)
//			{
//				result += i;
//			}
//		}
//		tStop = System.nanoTime();
//		System.out.println(result);
//		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		
		
		
		
		
		result = 0;
		
		tStart = System.nanoTime();
		for(int x = 0; x < DIM_X; x++)
		{
			for(int y = 0; y < DIM_Y; y++)
			{
				result += dim2[x][y];
			}
		}
		tStop = System.nanoTime();
		System.out.println(result);
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		
		
		
		
		
		
		
		
		
		result = 0;
		
		tStart = System.nanoTime();
		for(int x = 0; x < DIM_X; x++)
		{
			for(int y = 0; y < DIM_Y; y++)
			{
				result += dim1[x + DIM_Y*y];
			}
		}
		tStop = System.nanoTime();
		System.out.println(result);
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		
		
		

	}

}
