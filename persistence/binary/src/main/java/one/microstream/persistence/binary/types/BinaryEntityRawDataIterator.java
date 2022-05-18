package one.microstream.persistence.binary.types;

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

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;


public interface BinaryEntityRawDataIterator
{
	public long iterateEntityRawData(
		long                        startAddress      ,
		long                        boundAddress      ,
		BinaryEntityRawDataAcceptor entityDataAcceptor
	);
	
	
	public static BinaryEntityRawDataIterator New()
	{
		return new BinaryEntityRawDataIterator.Default();
	}
	
	public final class Default implements BinaryEntityRawDataIterator
	{
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public long iterateEntityRawData(
			final long                        startAddress      ,
			final long                        boundAddress      ,
			final BinaryEntityRawDataAcceptor entityDataAcceptor
		)
		{
			// the loop condition must be safe to read the item length
			final long itemStartBoundAddress = boundAddress - Binary.lengthLength() + 1;
			
			long address = startAddress;
			try
			{
				while(address < itemStartBoundAddress)
				{
					final long itemLength = XMemory.get_long(address);
					if(itemLength > 0)
					{
						// if the logic did not accept the entity data, iteration is aborted at the start of that entity.
						if(!entityDataAcceptor.acceptEntityData(address, boundAddress))
						{
							break;
						}
						
						// otherwise, the iteration advances to the next item (comment or entity)
						address += itemLength;
					}
					else if(itemLength < 0)
					{
						// comments (indicated by negative length) just get skipped.
						address -= itemLength;
					}
					else
					{
						// entity length may never be 0 or the iteration will hang forever
						throw new BinaryPersistenceException("Zero length data item.");
					}
				}
			}
			catch(final Exception e)
			{
				throw new BinaryPersistenceException(
					"Exception at address offset " + (address - startAddress)
					+ " (bound offset = " + (boundAddress - startAddress) + ")"
					, e
				);
			}
			
			// the total length of processed items is returned so the calling context can validate/advance/etc.
			return boundAddress - address;
		}
		
	}
	
	public static BinaryEntityRawDataIterator.Provider Provider()
	{
		return new BinaryEntityRawDataIterator.Provider.Default();
	}
	
	public interface Provider
	{
		public BinaryEntityRawDataIterator provideEntityDataIterator();
		
		public final class Default implements BinaryEntityRawDataIterator.Provider
		{
			@Override
			public BinaryEntityRawDataIterator provideEntityDataIterator()
			{
				return BinaryEntityRawDataIterator.New();
			}
			
		}
		
	}
		
}
