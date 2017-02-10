package net.jadoth.persistence.types;


/**
 * A type defining that an action to store an object graph can be performed.
 * For more complex (i.e. stateful transaction-like) storing, see {@link Storer}.
 *
 * @author TM
 */
public interface PersistenceStoring
{
	/* Note on naming:
	 * In contrast to the cache-assuming philosophy of the retrieving counterpart,
	 * the intention of storing is to always actually cause a storage action of some sort,
	 * not just putting an instance into a cache or such.
	 * Hence the naming here is a concrete "store" instead of just "put" or similar.
	 */

	/**
	 * Stores the passed instance and all referenced instances of persistable references recursively (fully deep).
	 * This is useful for storing all instances of an isolated sub-graph automatically, even if some of them are
	 * already known to the registry.
	 * Note, however, that depending on the data model, this can cause the whole enity graph to be stored on every call.
	 *
	 * @param instance the root instance of the subgraph to be stored.
	 * @return the object id representing the passed instances that was used to unswizzle it.
	 */
	public long storeFull(Object instance); // store complete graph, no matter what

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
	public long storeRequired(Object instance); // store passed instance in any case and required instances recursively

	/**
	 * Convenience method to {@link #storeFull(Object)} multiple instances.
	 *
	 * @param instances the root instances of the subgraphs to be stored.
	 * @return an array containing the object ids representing the passed instances that were used to unswizzle them.
	 */
	public long[] storeAllFull(Object... instances);

	/**
	 * Convenience method to {@link #storeRequired(Object)} multiple instances.
	 *
	 * @param instances the root instances of the subgraphs of required instances to be stored.
	 * @return an array containing the object ids representing the passed instances that were used to unswizzle them.
	 */
	public long[] storeAllRequired(Object... instances);


	/**
	 * The "natural" way of storing. By default, this method is just an alias for {@link #storeRequired(Object)}.
	 *
	 * @param instance the root instance of the subgraph of instances to be stored.
	 * @return
	 */
	public default long store(final Object instance)
	{
		return this.storeRequired(instance);
	}

	/**
	 * Convenience method to {@link #store(Object)} multiple instances.
	 *
	 * @param instances the root instances of the subgraphs of instances to be stored.
	 * @return an array containing the object ids representing the passed instances that were used to unswizzle them.
	 */
	public default long[] storeAll(final Object... instances)
	{
		return this.storeAllRequired(instances);
	}

}
