package one.microstream.collections.interfaces;

/**
 * 
 *
 */
public interface ExtendedCollection<E>
{
	// basic interface that contains all general procedures that are common to any type of extended collection

	// funnily, this is the only method (so far) common to both getting and adding concerns.
	public boolean nullAllowed();

	public boolean hasVolatileElements();


	public interface Creator<E, C extends ExtendedCollection<E>>
	{
		public C newInstance();
	}

}
