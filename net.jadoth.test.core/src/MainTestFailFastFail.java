import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;


public class MainTestFailFastFail
{
	public static void main(final String[] args)
	{
		final ArrayList<Integer> arrayList  = new ArrayList<>(Arrays.asList(1, 2, 3));

		try
		{
			for(final Integer i : arrayList)
			{
				System.out.println("processed element "+i);
				arrayList.remove(i);
			}
		}
		catch(final ConcurrentModificationException e)
		{
			System.out.println(arrayList);
			throw e;
		}

	}
}
