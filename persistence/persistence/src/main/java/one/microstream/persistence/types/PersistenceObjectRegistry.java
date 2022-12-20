package one.microstream.persistence.types;

import one.microstream.collections.Set_long;

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

import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashStatistics;
import one.microstream.persistence.internal.DefaultObjectRegistry;
import one.microstream.util.Cloneable;

/**
 * A registry type for biunique associations of arbitrary objects with ids.
 *
 * 
 */
public interface PersistenceObjectRegistry extends PersistenceSwizzlingLookup, Cloneable<PersistenceObjectRegistry>
{
	/* funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * welcome to this user code class
	 */
	
	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public PersistenceObjectRegistry Clone();
	
	// entry querying //

	@Override
	public long lookupObjectId(Object object);

	@Override
	public Object lookupObject(long objectId);
	
	public boolean isValid(long objectId, Object object);
	
	public void validate(long objectId, Object object);

	public boolean containsObjectId(long objectId);

	public <A extends PersistenceAcceptor> A iterateEntries(A acceptor);

	// general querying //

	public long size();

	public boolean isEmpty();

	public int hashRange();

	public float hashDensity();
	
	public long minimumCapacity();

	/**
	 * @return the current size potential before a (maybe costly) rebuild becomes necessary.
	 */
	public long capacity();
	
	public boolean setHashDensity(float hashDensity);
	
	public boolean setMinimumCapacity(long minimumCapacity);
	
	public boolean setConfiguration(float hashDensity, long  minimumCapacity);
	
	/**
	 * Makes sure the internal storage structure is prepared to provide a {@link #capacity()} of at least
	 * the passed capacity value.
	 * 
	 * @param capacity the new minimum capacity
	 * @return whether a rebuild of internal storage structures was necessary.
	 */
	public boolean ensureCapacity(long capacity);
		
	// registering //
	
	public boolean registerObject(long objectId, Object object);

	public Object optionalRegisterObject(long objectId, Object object);
	
	public boolean registerConstant(long objectId, Object constant);

	/**
	 * Consolidate internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 * 
	 * @return whether a rebuild was required.
	 */
	public boolean consolidate();

	/**
	 * Clears all entries except those that are essential for a correctly executed program (e.g. constants). <br>
	 * Clearing means to leave the current capacity as it is and just to actually clear its entries.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void clear();

	/**
	 * Clears all entries, including those that are essential for a correctly executed program (e.g. constants),
	 * effectively leaving a completely empty registry.<br>
	 * Clearing means to leave the current capacity as it is and just to actually clear its entries.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void clearAll();
	
	/**
	 * Truncates all entries except those that are essential for a correctly executed program (e.g. constants).
	 * Truncating means to quickly empty the registry by reinitializing the internal storage structures with a
	 * new and minimal capacity.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void truncate();

	/**
	 * Truncates all entries, including those that are essential for a correctly executed program (e.g. constants),
	 * effectively leaving a completely empty registry.<br>
	 * Truncating means to quickly empty the registry by reinitializing the internal storage structures with a
	 * new and minimal capacity.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void truncateAll();
	
	// removing logic is not viable except for testing purposes, which can be done implementation-specific.
	
	public XGettingTable<String, ? extends HashStatistics> createHashStatistics();
	
	/**
	 * 
	 * @param processor the object id processor
	 * @return <code>true</code> on success, <code>false</code> if lock rejected.
	 */
	public boolean processLiveObjectIds(ObjectIdsProcessor processor);

	// for bulk processing of objectIds. Most efficient way for server mode, inefficient for embedded mode.
	/**
	 * 
	 * @param objectIdsBaseSet the ids to select
	 * @return null if lock rejected
	 */
	public Set_long selectLiveObjectIds(Set_long objectIdsBaseSet);
	
	
	
	public static DefaultObjectRegistry New()
	{
		return DefaultObjectRegistry.New();
	}
	
}
