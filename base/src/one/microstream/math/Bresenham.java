package one.microstream.math;

import java.awt.Point;

public class Bresenham
{

	/**
	 * Determines the amount of discrete steps from (x1,y1) to (x2,y2), where one step is a change of coordinates
	 * in either straight or diagonal direction.<p>
	 * Examples:<br>
	 * (0,0) to (2,0) = 2 steps<br>
	 * (0,0) to (2,2) = 2 steps<br>
	 * (5,18) to (10,9) = 9 steps<br>
	 *
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static final int stepCountDistance(final int x1, final int y1, final int x2, final int y2)
	{
		return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
	}

	public static final Point[] linePoints(final int x1, final int y1, final int x2, final int y2)
	{
		int x = x1, y = y1, d = 0, hx = x2 - x1, hy = y2 - y1, c, m, xInc = 1, yInc = 1;
		final Point[] points = new Point[Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) + 1];
		int idx = 0;

		if(hx < 0)
		{
			xInc = -1;
			hx = -hx;
		}
		if(hy < 0)
		{
			yInc = -1;
			hy = -hy;
		}
		if(hy <= hx)
		{
			c = 2 * hx;
			m = 2 * hy;
			while(true)
			{
				points[idx++] = new Point(x, y);
				if(x == x2)
				{
					break;
				}
				x += xInc;
				d += m;
				if(d > hx)
				{
					y += yInc;
					d -= c;
				}
			}
		}
		else
		{
			c = 2 * hy;
			m = 2 * hx;
			while(true)
			{
				points[idx++] = new Point(x, y);
				if(y == y2)
				{
					break;
				}
				y += yInc;
				d += m;
				if(d > hy)
				{
					x += xInc;
					d -= c;
				}
			}
		}
		return points;
	}

	public static final int[] linePointsInt1D(final int x1, final int y1, final int x2, final int y2)
	{
		int x = x1, y = y1, d = 0, hx = x2 - x1, hy = y2 - y1, c, m, xInc = 1, yInc = 1;
		final int[] points = new int[(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) + 1) * 2];
		int idx = 0;

		if(hx < 0)
		{
			xInc = -1;
			hx = -hx;
		}
		if(hy < 0)
		{
			yInc = -1;
			hy = -hy;
		}
		if(hy <= hx)
		{
			c = 2 * hx;
			m = 2 * hy;
			while(true)
			{
				points[idx++] = x;
				points[idx++] = y;
				if(x == x2)
				{
					break;
				}
				x += xInc;
				d += m;
				if(d > hx)
				{
					y += yInc;
					d -= c;
				}
			}
		}
		else
		{
			c = 2 * hy;
			m = 2 * hx;
			while(true)
			{
				points[idx++] = x;
				points[idx++] = y;
				if(y == y2)
				{
					break;
				}
				y += yInc;
				d += m;
				if(d > hy)
				{
					x += xInc;
					d -= c;
				}
			}
		}
		return points;
	}

	public static final int[][] linePointsInt2D(final int x1, final int y1, final int x2, final int y2)
	{
		int x = x1, y = y1, d = 0, hx = x2 - x1, hy = y2 - y1, c, m, xInc = 1, yInc = 1;
		final int[][] points = new int[Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) + 1][];
		int idx = 0;

		if(hx < 0)
		{
			xInc = -1;
			hx = -hx;
		}
		if(hy < 0)
		{
			yInc = -1;
			hy = -hy;
		}
		if(hy <= hx)
		{
			c = 2 * hx;
			m = 2 * hy;
			while(true)
			{
				points[idx++] = new int[]{x, y};
				if(x == x2)
				{
					break;
				}
				x += xInc;
				d += m;
				if(d > hx)
				{
					y += yInc;
					d -= c;
				}
			}
		}
		else
		{
			c = 2 * hy;
			m = 2 * hx;
			while(true)
			{
				points[idx++] = new int[]{x, y};
				if(y == y2)
				{
					break;
				}
				y += yInc;
				d += m;
				if(d > hy)
				{
					x += xInc;
					d -= c;
				}
			}
		}
		return points;
	}

	public static final void line(
		final int x1, final int y1, final int x2, final int y2, final IntCoordinateManipulator manipulator
	)
		throws InvalidCoordinateException
	{
		int x = x1, y = y1, d = 0, hx = x2 - x1, hy = y2 - y1, c, m, xInc = 1, yInc = 1;

		if(hx < 0)
		{
			xInc = -1;
			hx = -hx;
		}
		if(hy < 0)
		{
			yInc = -1;
			hy = -hy;
		}
		if(hy <= hx)
		{
			c = 2 * hx;
			m = 2 * hy;
			while(true)
			{
				manipulator.manipulateCoordinate(x, y);
				if(x == x2)
				{
					break;
				}
				x += xInc;
				d += m;
				if(d > hx)
				{
					y += yInc;
					d -= c;
				}
			}
		}
		else
		{
			c = 2 * hy;
			m = 2 * hx;
			while(true)
			{
				manipulator.manipulateCoordinate(x, y);
				if(y == y2)
				{
					break;
				}
				y += yInc;
				d += m;
				if(d > hy)
				{
					x += xInc;
					d -= c;
				}
			}
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private Bresenham()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
