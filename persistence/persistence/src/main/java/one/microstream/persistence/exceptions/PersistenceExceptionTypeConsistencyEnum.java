package one.microstream.persistence.exceptions;

/*-
 * #%L
 * MicroStream Persistence
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

public class PersistenceExceptionTypeConsistencyEnum extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public PersistenceExceptionTypeConsistencyEnum(final String constantName, final String enumClassName, final int ordinal, final long targetOrdinal)
	{
		super(
			buildMessage(
				constantName,
				enumClassName,
				ordinal,
				targetOrdinal
		));
	}
	
	private static String buildMessage(final String constantName, final String enumClassName, final int ordinal, final long targetOrdinal)
	{
		return "The ordinal of the enum constant " + constantName + " of " +
			enumClassName +
			"\nwould be change by the legacy type mapping from " +
			ordinal + " to " + targetOrdinal + ". This may cause the storage becoming corrupted." +
			"\nIf the ordinal change is intended you need to define a manual legacy type mapping!";
	}
}
	
