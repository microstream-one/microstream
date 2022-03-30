package one.microstream.util.traversing;

/*-
 * #%L
 * microstream-base
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


public final class TraversalSignalAbort extends AbstractTraversalSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// can be thrown any number of times, so a singleton instead of constant instantiation is the better approach
	static final TraversalSignalAbort SINGLETON = new TraversalSignalAbort();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Should actually be called "throw", but that is a keyword.
	 */
	public static void fire() throws TraversalSignalAbort
	{
		throw SINGLETON;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private TraversalSignalAbort()
	{
		super();
	}
		
}
