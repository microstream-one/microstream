package one.microstream.persistence.binary.exceptions;

/*-
 * #%L
 * microstream-persistence-binary
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

public class BinaryPersistenceExceptionInvalidListElements extends BinaryPersistenceExceptionInvalidList
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long listElementCount;
	private final long elementLength   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionInvalidListElements(
		final long entityLength    ,
		final long objectId        ,
		final long typeId          ,
		final long listStartOffset ,
		final long listTotalLength ,
		final long listElementCount,
		final long elementLength
	)
	{
		super(entityLength, objectId, typeId, listStartOffset, listTotalLength);
		this.listElementCount = listElementCount;
		this.elementLength    = elementLength   ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected String assembleDetailStringBody()
	{
		return super.assembleDetailStringBody() + ", " +
			"listElementCount = " + this.listElementCount + ", " +
			"elementLength = "    + this.elementLength
		;
	}
	
}
