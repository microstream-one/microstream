package one.microstream.persistence.exceptions;

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



public class PersistenceExceptionConsistencyWrongTypeId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<?> type     ;
	final long     actualTid;
	final long     passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyWrongTypeId(final Class<?> type, final long actualTid, final long passedTid)
	{
		super();
		this.type      = type     ;
		this.actualTid = actualTid;
		this.passedTid = passedTid;
	}

	@Override
	public String getMessage()
	{
		return "Wrong type id for " + this.type + ": actual tid: " + this.actualTid + ", passed tid: " + this.passedTid
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
