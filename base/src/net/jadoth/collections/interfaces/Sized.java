package net.jadoth.collections.interfaces;

/**
 * @author Thomas Muenz
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
