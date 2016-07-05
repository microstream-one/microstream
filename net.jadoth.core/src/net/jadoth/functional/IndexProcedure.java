package net.jadoth.functional;

/**
 * @author Thomas Muenz
 *
 */
public interface IndexProcedure<T>
{
	public void accept(T e, long index);
}
