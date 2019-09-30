package various;
/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class Superclass
{
	@SuppressWarnings("unused")
	private int value;

	private final SubClass sc = new SubClass();
	{
		((Superclass)this.sc).value = 0;
	}


	static class SubClass extends Superclass
	{
		// empty
	}
}



