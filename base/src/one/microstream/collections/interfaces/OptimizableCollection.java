package one.microstream.collections.interfaces;

/**
 * @author Thomas Muenz
 *
 */
public interface OptimizableCollection extends Sized
{
	/**
	 * Optimizes the internal storage of this collection and returns the storage size of the collection after the
	 * process is complete.
	 *
	 * @return the storage size of the collection after the optimzation.
	 */
	public long optimize();

}
