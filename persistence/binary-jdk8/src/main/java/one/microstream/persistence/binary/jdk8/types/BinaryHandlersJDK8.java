package one.microstream.persistence.binary.jdk8.types;

/*-
 * #%L
 * microstream-persistence-binary-jdk8
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

import one.microstream.X;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerArrayList;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerHashMap;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerHashSet;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerHashtable;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashMap;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashSet;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerProperties;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerStack;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerVector;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistration;

public final class BinaryHandlersJDK8
{
	public static <F extends PersistenceTypeHandlerRegistration.Executor<Binary>> F registerJDK8TypeHandlers(final F executor)
	{
		executor.executeTypeHandlerRegistration((r, c) ->
			r.registerTypeHandlers(X.List(
				// JDK 1.0 collections
				BinaryHandlerVector.New(c)      ,
				BinaryHandlerHashtable.New()    ,
				BinaryHandlerStack.New(c)       ,
				BinaryHandlerProperties.New()   ,
				
				// JDK 1.2 collections
				BinaryHandlerArrayList.New(c)   ,
				BinaryHandlerHashSet.New()      ,
				BinaryHandlerHashMap.New()      ,
				
				// JDK 1.4 collections
				BinaryHandlerLinkedHashMap.New(),
				BinaryHandlerLinkedHashSet.New()
			))
		);
		
		return executor;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	protected BinaryHandlersJDK8()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
