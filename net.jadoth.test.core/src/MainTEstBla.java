/**
 * 
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTEstBla
{
	public static void main(final String[] args)
	{
		int i = 10;
		
		final Object[] array = new Object[i];
		
		System.out.println(i);
		try
		{
			array[i++] = null;
		}
		catch(final Exception e)
		{
			// TODO: Auto-generated: handle exception
		}
		System.out.println(i);
	}
}
