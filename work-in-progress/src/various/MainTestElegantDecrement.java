package various;
import one.microstream.X;

public class MainTestElegantDecrement
{
	public static void main(final String[] args)
	{
		final int[] array = X.ints(4,5,6,7);
		
		// "for i from array length going to zero, do:"
		for(int i = array.length; i --> 0;)
		{
			System.out.println(array[i]);
		}
				
		int i = array.length; //already existing length variable
		
		// "while i is going to zero, do:"
		while(i --> 0)
		{
			System.out.println(array[i]);
		}
	}

}
