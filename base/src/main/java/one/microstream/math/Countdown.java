package one.microstream.math;

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

/**
 * Class that wraps a combination of start value and remaining value to implement a countdown.<br>
 * The countdown can be decremented, resetted and be checked for having expired or still being active.
 */
public class Countdown
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final int startValue;
	private int remainingValue;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Instantiates a new countdown with a given start value.
	 * @param startValue may not be negative
	 */
	public Countdown(final int startValue)
	{
		super();
		if(startValue < 0)
		{
			throw new IllegalArgumentException("Countdown value may not be negative: " + startValue);
		}
		this.startValue = startValue;
		this.remainingValue = startValue;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////
	
	/**
	 * @return the start value of this countdown. Cannot be negative.
	 */
	public int getStartValue()
	{
		return this.startValue;
	}
	
	/**
	 * @return the remaining value of this countdown. Will always be in the range [0;startValue]
	 */
	public int getRemainingValue()
	{
		return this.remainingValue;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	/**
	 * @return a String representing the plain remaining value of this countdown.
	 */
	@Override
	public String toString()
	{
		return Integer.toString(this.remainingValue);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////
	
	/**
	 * Decrements this countdown by 1.
	 * 
	 * @return 1 if the countdown already expired, 0 otherwise (returns the unconsumed amount of value 1)
	 * @see #decrease(int)
	 */
	public int decrement()
	{
		if(this.remainingValue == 0)
		{
			return 1;
		}
		this.remainingValue--;
		return 0;
	}
	
	/**
	 * @param offset the offset to decrease this countdown with
	 * @return the unconsumed amount of {@code offset} due to preterm expiration of the countdown.
	 * @see #decrement()
	 */
	public int decrease(final int offset)
	{
		if(offset < 0)
		{
			throw new IllegalArgumentException(
				this.getClass().getSimpleName() + " decrease offset may not be negative: " + offset
			);
		}
		
		final int value = this.remainingValue;
		if(value == 0)
		{
			//consume offset not at all, return unchanged
			return offset;
		}
		
		if(offset > value)
		{
			//consume offset partially, return rest
			this.remainingValue = 0;
			return offset - value;
		}
		
		//consume offset completely, return 0
		this.remainingValue = value - offset;
		return 0;
	}
	
	/**
	 * Sets this countdown's remaining value to the given value and return its preceding value.
	 * @param newValue the new value for {@code remainingValue}
	 * @return the value that {@code remainingValue} had so far
	 */
	private int manipulateRemainingValue(final int newValue)
	{
		final int value = this.remainingValue;
		this.remainingValue = newValue;
		return value;
	}
	
	/**
	 * Resets this countdown to its start value.
	 * @return the remaining countdown value before the reset.
	 */
	public int reset()
	{
		return this.manipulateRemainingValue(this.startValue);
	}
	
	/**
	 * Immediately expires this countdown.
	 * @return the remaining countdown value before the expiration.
	 */
	public int expire()
	{
		return this.manipulateRemainingValue(0);
	}
	
	/**
	 * Tells if this countdown is still active (meaning its remaining value is greater than 0)
	 * @return <code>true</code> if this countdown's remaining value is greater 0, <code>false</code> otherwise.
	 * @see #isAtStart()
	 * @see #isExpired()
	 */
	public boolean isActive()
	{
		return this.remainingValue > 0;
	}
	
	/**
	 * Tells if this countdown is expired(meaning its remaining value is 0)
	 * @return <code>true</code> if this countdown's remaining value 0, <code>false</code> otherwise.
	 * @see #isActive()
	 * @see #isAtStart()
	 */
	public boolean isExpired()
	{
		return this.remainingValue == 0;
	}
	
	/**
	 * Tells if this countdown is still full (meaning its remaining value is equal to its start value)
	 * @return <code>true</code> if this countdown's remaining value is the same as its start value,
	 * <code>false</code> otherwise.
	 * @see #isActive()
	 * @see #isExpired()
	 */
	public boolean isAtStart()
	{
		return this.remainingValue == this.startValue;
	}
	

	
}
