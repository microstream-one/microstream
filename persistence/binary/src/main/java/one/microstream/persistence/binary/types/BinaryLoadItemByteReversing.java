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

public final class BinaryLoadItemByteReversing extends BinaryLoadItem
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLoadItemByteReversing(final long entityContentAddress)
	{
		super(entityContentAddress);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean isSwitchedByteOrder()
	{
		return true;
	}

	@Override
	final short get_shortfromAddress(final long address)
	{
		return Short.reverseBytes(XMemory.get_short(address));
	}

	@Override
	final char get_charFromAddress(final long address)
	{
		return Character.reverseBytes(XMemory.get_char(address));
	}

	@Override
	final int get_intFromAddress(final long address)
	{
		return Integer.reverseBytes(XMemory.get_int(address));
	}

	@Override
	final float get_floatFromAddress(final long address)
	{
		// this is tricky
		return Float.intBitsToFloat(Integer.reverseBytes(XMemory.get_int(address)));
	}

	@Override
	final long get_longFromAddress(final long address)
	{
		return Long.reverseBytes(XMemory.get_long(address));
	}

	@Override
	final double get_doubleFromAddress(final long address)
	{
		// this is tricky
		return Double.longBitsToDouble(Long.reverseBytes(XMemory.get_long(address)));
	}
	
	@Override
	final void set_shortToAddress(final long address, final short value)
	{
		XMemory.set_short(address, Short.reverseBytes(value));
	}
	
	@Override
	final void set_charToAddress(final long address, final char value)
	{
		XMemory.set_char(address, Character.reverseBytes(value));
	}
	
	@Override
	final void set_intToAddress(final long address, final int value)
	{
		XMemory.set_int(address, Integer.reverseBytes(value));
	}
	
	@Override
	final void set_floatToAddress(final long address, final float value)
	{
		XMemory.set_int(address, Integer.reverseBytes(Float.floatToRawIntBits(value)));
	}
	
	@Override
	final void set_longToAddress(final long address, final long value)
	{
		XMemory.set_long(address, Long.reverseBytes(value));
	}
	
	@Override
	final void set_doubleToAddress(final long address, final double value)
	{
		XMemory.set_long(address, Long.reverseBytes(Double.doubleToRawLongBits(value)));
	}
	
	

	@Override
	final void update_shortsFromAddress(final long address, final short[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.get_shortfromAddress(address + i * Short.BYTES);
		}
	}

	@Override
	final void update_charsFromAddress(final long address, final char[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.get_charFromAddress(address + i * Character.BYTES);
		}
	}

	@Override
	final void update_intsFromAddress(final long address, final int[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.get_intFromAddress(address + i * Integer.BYTES);
		}
	}

	@Override
	final void update_floatsFromAddress(final long address, final float[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.get_floatFromAddress(address + i * Float.BYTES);
		}
	}

	@Override
	public final void update_longsFromAddress(final long address, final long[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.get_longFromAddress(address + i * Long.BYTES);
		}
	}

	@Override
	final void update_doublesFromAddress(final long address, final double[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.get_doubleFromAddress(address + i * Double.BYTES);
		}
	}
	
	@Override
	final void store_shortsToAddress(final long address, final short[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.set_shortToAddress(address + i * Short.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_charsToAddress(final long address, final char[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.set_charToAddress(address + i * Character.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_intsToAddress(final long address, final int[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.set_intToAddress(address + i * Integer.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_floatsToAddress(final long address, final float[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.set_floatToAddress(address + i * Float.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_longsToAddress(final long address, final long[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.set_longToAddress(address + i * Long.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_doublesToAddress(final long address, final double[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.set_doubleToAddress(address + i * Double.BYTES, values[i]);
		}
	}
	
	@Override
	final void storeEntityHeaderToAddress(
		final long entityAddress    ,
		final long entityTotalLength,
		final long entityTypeId     ,
		final long entityObjectId
	)
	{
		setEntityHeaderRawValuesToAddress(
			entityAddress,
			Long.reverseBytes(entityTotalLength),
			Long.reverseBytes(entityTypeId),
			Long.reverseBytes(entityObjectId)
		);
	}
	
}
