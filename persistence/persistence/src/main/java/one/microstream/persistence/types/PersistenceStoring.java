package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */


/**
 * A type defining that an action to store an object graph can be performed.
 * For more complex (i.e. stateful transaction-like) storing, see {@link Storer}.
 *
 * 
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
	 * @return the object id representing the passed instance.
	 */
	public long store(Object instance); // store passed instance in any case and required instances recursively
	
	/**
	 * Convenience method to {@link #store(Object)} multiple instances.
	 * The passed array (maybe implicitely created by the compiler) itself is NOT stored.
	 *
	 * @param instances multiple root instances of the subgraphs of required instances to be stored.
	 * @return an array containing the object ids representing the passed instances.
	 */
	/* (09.11.2018 TM)NOTE: change from "store" to "storeAll".
	 * While the prior would be more convenient, it has one critical loophole:
	 * When a (non-primitive) array shall be stored (as an instance by itself via the non-array store()),
	 * the compiler would still choose this method, resulting in the array instance itself NOT being stored,
	 * but only its content.
	 * This error is very hard to spot, even for experienced developers and almost impossible to spot and hard
	 * to understand for novice developers.
	 * Therefore, it is necessary to exchange convenience for safety and rename the method sooner rather than later.
	 */
	public long[] storeAll(Object... instances);
	
	
	/**
	 * Convenience method to {@link #store(Object)} all instances of an {@link Iterable} type, usually a collection.<br>
	 * The passed instance itself is NOT stored.<br>
	 * Note that this method does not return an array of objectIds, since the amount of instances supplied by the
	 * passed {@link Iterable} cannot be known until after all instances have been stored and the memory and performance
	 * overhead to collect them dynamically would not be worth it in most cases since the returned array is hardly ever
	 * needed.
	 * If it should be needed, the desired behavior can be easily achieved with a tiny custom-made utility method.
	 * 
	 * @param instances multiple root instances of the subgraphs of required instances to be stored.
	 */
	public void storeAll(Iterable<?> instances);
	
}
