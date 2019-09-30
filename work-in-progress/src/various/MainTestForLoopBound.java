package various;
/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTestForLoopBound
{
	private int size;
	private int[] ints;

	void test()
	{
		long tStart, tStop;
		int sum;

		final int dir = 1;


		for(int n = 5; n --> 0;)
		{
			final int[] ints = this.ints;
			sum = 0;

			final int startIndex = 10, endIndex = this.size-10;
//			final int bound = endIndex+dir;
			tStart = System.nanoTime();

//			for(int i = 0, size = this.size; i < size; i++)
//			{
//			for(int i = 0, size = ints.length; i < size; i++)
//			{
//			for(int i = 0; i < ints.length; i++)
//			{
//			for(int i = 0; i < this.ints.length; i++)
//			{
//			for(int i = 0; i < this.size; i++)
//			{
//			for(int i = 0; i < this.size; i+=dir)
//			{
//			for(int i = this.size-1; i > 0; i-=dir)
//			{
			for(int i = startIndex, bound = endIndex+dir; i != bound; i+=dir)
			{
//			for(int i = startIndex; i != bound; i+=dir)
//			{
//			for(int i = endIndex; i != startIndex; i+=dir)
//			{
				sum += ints[i];
//				sum += this.ints[i];
			}

//			for(final int i : this.ints)
//			{
//				sum += i;
//			}

			tStop = System.nanoTime();
			System.out.println(sum);
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}


	public static void main(final String[] args)
	{
		final MainTestForLoopBound test = new MainTestForLoopBound();

		final int size = 10 * 1000 * 1000;

		final int ints[] = new int[size];
		for(int i = 0; i < ints.length; i++)
		{
			ints[i] = i;
		}

		test.size = size;
		test.ints = ints;

		test.test();
	}
}
