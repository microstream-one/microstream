package one.microstream.persistence.binary.jdk17.types;

/*-
 * #%L
 * MicroStream Persistence JDK17
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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
import one.microstream.persistence.binary.jdk17.java.util.BinaryHandlerImmutableCollectionsList12;
import one.microstream.persistence.binary.jdk17.java.util.BinaryHandlerImmutableCollectionsSet12;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistration;

public final class BinaryHandlersJDK17
{
	public static <F extends PersistenceTypeHandlerRegistration.Executor<Binary>> F registerJDK17TypeHandlers(final F executor)
	{
		executor.executeTypeHandlerRegistration((r, c) ->
			r.registerTypeHandlers(X.List(
				BinaryHandlerImmutableCollectionsSet12.New(),
				BinaryHandlerImmutableCollectionsList12.New()
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
	protected BinaryHandlersJDK17()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
