package various;
import one.microstream.equality.Equalator;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTestEqualator
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final Integer i = 5;
		final Long l = 5L;

		System.out.println(EQUAL_NUMBER.equal(i, l));

	}


	static final Equalator<Number> EQUAL_NUMBER = (final Number s1, final Number s2) ->
	{
		if(s1 == s2)
		{
			return true;
		}
		if(s1 == null)
		{
			return s2 == null;
		}
		return s1.equals(s2);
	};

}
