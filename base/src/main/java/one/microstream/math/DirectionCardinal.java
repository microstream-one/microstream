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

public enum DirectionCardinal
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// CHECKSTYLE.OFF: MagicNumber: these are virtually already constants.
	NORTH_WEST(1 << 4), NORTH(1 << 0), NORTH_EAST(1 << 5),

	WEST      (1 << 3), CENTER(0)    , EAST      (1 << 1),

	SOUTH_WEST(1 << 7), SOUTH(1 << 2), SOUTH_EAST(1 << 6);
	// CHECKSTYLE.ON: MagicNumber



	private static final DirectionCardinal[][] MATRIX =
	{
		{NORTH_WEST, NORTH,  NORTH_EAST},
		{WEST,       CENTER, EAST      },
		{SOUTH_WEST, SOUTH,  SOUTH_EAST}
	};



	/**
	 *
	 * @param difX a value in { - 1, 0, +1}
	 * @param difY a value in { - 1, 0, +1}
	 * @return the direction {@code difX} or {@code difY} represent or {@code null} otherweise.
	 * @throws IllegalArgumentException if {@code (difX, difY)} does not point to a neighbor square
	 */
	public static final DirectionCardinal translateNeighborVector(final int difX, final int difY)
		throws IllegalArgumentException
	{
		if(difX == 0 && difY == 0 || Math.abs(difX) != 1 && Math.abs(difY) != 1)
		{
			throw new IllegalArgumentException("Vector does not point to a neighbor: (" + difX + ',' + difY + ')');
		}
		return MATRIX[difY + 1][difX + 1];
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final     int               directionBit  ;
	private transient DirectionCardinal cachedOpposite;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	private DirectionCardinal(final int bit)
	{
		this.directionBit = bit;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public int getBit()
	{
		return this.directionBit;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private final DirectionCardinal determineOpposite()
	{
		switch(this)
		{
			case NORTH_WEST: return SOUTH_EAST;
			case NORTH:      return SOUTH;
			case NORTH_EAST: return SOUTH_WEST;
			case WEST:       return EAST;
			case EAST:       return WEST;
			case SOUTH_WEST: return NORTH_EAST;
			case SOUTH:      return NORTH;
			case SOUTH_EAST: return NORTH_WEST;
			case CENTER:     return CENTER;
			default:
				//can never occur unless someone adds a direction (?) without modifying this switch
				throw new RuntimeException("Unknown " + DirectionCardinal.class.getSimpleName() + ": " + this);
		}
	}

	public DirectionCardinal opposite()
	{
		if(this.cachedOpposite == null)
		{
			this.cachedOpposite = this.determineOpposite();
		}
		return this.cachedOpposite;
	}

	public final boolean isSet(final int bits)
	{
		return (bits & this.directionBit) > 0;
	}

	public final boolean isMainDirection()
	{
		final int bit = this.directionBit;
		return bit != 0 && bit <= WEST.directionBit;
	}
	public final boolean isIntermediateDirection()
	{
		final int bit = this.directionBit;
		return bit != 0 && bit > WEST.directionBit;
	}

}
