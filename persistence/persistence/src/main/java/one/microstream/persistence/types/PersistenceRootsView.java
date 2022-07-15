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

import java.util.function.BiConsumer;


/**
 * Instances of this type represent a read-only view on all technical root instances present in a persistence context.
 * <p>
 * Note that while there might only one application root instance, there must a lot of other instances registered
 * as roots on a technical level, as well. Every instance that is publicly accessible through
 * a static context ("constant instances") must technically considered to be a "root" for the persistent context.
 * This includes all enum intances. The entirety of all technical root instances can be iterated or "viewed" via
 * an instance of this type.
 * 
 * 
 */
public interface PersistenceRootsView
{
	
	public static String rootIdentifier()
	{
		// just a convenience relaying method.
		return Persistence.rootIdentifier();
	}
	
	// (16.12.2019 TM)NOTE: no deprecated methods in non-convenience API (while still in early versions ...)
	
//	@Deprecated
//	public default String defaultRootIdentifier()
//	{
//		return Persistence.defaultRootIdentifier();
//	}
//
//	@Deprecated
//	public default Referencing<Object> defaultRoot()
//	{
//		return this.root();
//	}
//
//	@Deprecated
//	public default String customRootIdentifier()
//	{
//		return Persistence.customRootIdentifier();
//	}
//
//	@Deprecated
//	public default Object customRoot()
//	{
//		final PersistenceRootReferencing rootReference = this.root();
//
//		return rootReference != null
//			? rootReference.get()
//			: null
//		;
//	}
	
	public PersistenceRootReferencing rootReference();
		
	public <C extends BiConsumer<String, Object>> C iterateEntries(C iterator);
}
