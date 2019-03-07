package net.jadoth.collections.interfaces;

/**
 * @author Thomas Muenz
 *
 */
public interface OptimizableCollection
{
	/**
	 * Optimizes the internal storage of this collection and returns the storage size of the collection after the
	 * process is complete.
	 *
	 * @return the storage size of the collection after the optimzation.
	 */
	public long optimize();

}
