package one.microstream.collections.interfaces;

/**
 * 
 *
 */
public interface Sized
{
	public long size();

	public default boolean isEmpty()
	{
		return this.size() == 0L;
	}

}
