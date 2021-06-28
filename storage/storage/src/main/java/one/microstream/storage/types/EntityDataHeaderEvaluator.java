package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.persistence.binary.types.Binary;

public final class EntityDataHeaderEvaluator
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long
		lengthLowerValue  ,
		lengthUpperBound  ,
		typeIdLowerValue  ,
		typeIdUpperBound  ,
		objectIdLowerValue,
		objectIdUpperBound
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	EntityDataHeaderEvaluator(
		final long lengthLowerValue  ,
		final long lengthUpperBound  ,
		final long typeIdLowerValue  ,
		final long typeIdUpperBound  ,
		final long objectIdLowerValue,
		final long objectIdUpperBound
	)
	{
		super();
		this.lengthLowerValue   = lengthLowerValue  ;
		this.lengthUpperBound   = lengthUpperBound  ;
		this.typeIdLowerValue   = typeIdLowerValue  ;
		this.typeIdUpperBound   = typeIdUpperBound  ;
		this.objectIdLowerValue = objectIdLowerValue;
		this.objectIdUpperBound = objectIdUpperBound;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static boolean isValid(final long lowerValue, final long upperBound, final long value)
	{
		if(value < lowerValue)
		{
			return false;
		}
		if(value >= upperBound)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean isValidHeader(final long entityStartAddress, final long availableDataLength)
	{
		if(Binary.entityHeaderLength() > availableDataLength)
		{
			return false;
		}
		final long length   = Binary.getEntityLengthRawValue(entityStartAddress)  ;
		final long typeId   = Binary.getEntityTypeIdRawValue(entityStartAddress)  ;
		final long objectId = Binary.getEntityObjectIdRawValue(entityStartAddress);
		
		if(!this.isValidHeader(length, typeId, objectId))
		{
			return false;
		}

		if(length > availableDataLength)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean isValidHeader(final long length, final long typeId, final long objectId)
	{
		if(!isValid(this.lengthLowerValue, this.lengthUpperBound, length))
		{
			return false;
		}
		if(!isValid(this.typeIdLowerValue, this.typeIdUpperBound, typeId))
		{
			return false;
		}
		if(!isValid(this.objectIdLowerValue, this.objectIdUpperBound, objectId))
		{
			return false;
		}
		
		return true;
	}
	
}
