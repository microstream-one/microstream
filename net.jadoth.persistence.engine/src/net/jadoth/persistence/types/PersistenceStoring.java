package net.jadoth.persistence.types;


/**
 * A type defining that an action to store an object graph can be performed.
 * For more complex (i.e. stateful transaction-like) storing, see {@link Storer}.
 *
 * @author TM
 */
public interface PersistenceStoring
{
	/**
	 * Stores the passed instance in any case and all referenced instances of persistable references recursively,
	 * but stores referenced instances only if they are newly encountered (e.g. don't have an id associated with
	 * them in the object registry, yet and are therefore required to be handled).
	 * This is useful for the common case of just storing an updated instance and potentially newly created
	 * instances along with it while skipping all existing (and normally unchanged) referenced instances.<p>
	 *
	 * @param instance the root instance of the subgraph of required instances to be stored.
	 * @return the object id representing the passed instances that was used to unswizzle it.
	 */
	public long store(Object instance); // store passed instance in any case and required instances recursively
	
	/**
	 * Convenience method to {@link #store(Object)} multiple instances.
	 *
	 * @param instances the root instances of the subgraphs of required instances to be stored.
	 * @return an array containing the object ids representing the passed instances that were used to unswizzle them.
	 */
	public long[] storeAll(Object... instances);

}
