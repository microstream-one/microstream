/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTestClassPseudoRandom8
{
	static final int[] ints = new int[8];
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{

		while(true)
		{
			int i = (int)(System.nanoTime() & 7);
			ints[i] = ints[i] + 1;
			printInts();
		}

	}

	static void printInts()
	{
		for(int i : ints)
		{
			System.out.print(i/1000);
			System.out.print('\t');
		}
		System.out.println("");
	}

}
