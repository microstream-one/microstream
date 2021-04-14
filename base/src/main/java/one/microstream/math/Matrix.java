/**
 *
 */
package one.microstream.math;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * 
 *
 */
public class Matrix<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Validates if {@code array} is a well-formed array in terms of a matrix.
	 * <p>
	 * Well-formed arrays meet the following criteria:<br>
	 * - Both dimensions have a size greater than 0.<br>
	 * - No nested array may be null.<br>
	 * - All nested arrays have the same size.<br>
	 *
	 * @param <T>
	 * @param array
	 * @throws MatrixException
	 * @throws NullPointerException
	 */
	public static <T> T[][] validateArray(final T[][] array) throws MatrixException, NullPointerException
	{
		if(array == null)
		{
			throw new NullPointerException("array is null");
		}
		if(array.length == 0)
		{
			throw new MatrixException("Array may not have length 0");
		}

		int i = 0;
		try
		{
			final int nestedArraysLength = array[i].length;
			if(nestedArraysLength == 0)
			{
				throw new MatrixException("Nested arrays may not have length 0");
			}
			while(i < array.length)
			{
				final Object[] objects = array[i];
				if(objects.length != nestedArraysLength)
				{
					throw new MatrixException(
						"array has different sized nested array at index" + i + ": "
						+ objects.length + " != " + nestedArraysLength
					);
				}
				i++;
			}
			return array;
		}
		catch(final NullPointerException e)
		{
			throw new MatrixException("matrix array has no nested array at index " + i);
		}
	}



	private static final void validateRowIndex(final int index, final Object[][] data, final String type)
		throws ArrayIndexOutOfBoundsException
	{
		if(index < 0)
		{
			throw new ArrayIndexOutOfBoundsException(type + " may not be negative: " + index);
		}
		if(index >= data.length)
		{
			throw new ArrayIndexOutOfBoundsException(type + " out of bounds: " + index + " >= " + data.length);
		}
	}
	private static final void validateColumnIndex(final int index, final Object[] data, final String type)
		throws ArrayIndexOutOfBoundsException
	{
		if(index < 0)
		{
			throw new ArrayIndexOutOfBoundsException(type + " may not be negative: " + index);
		}
		if(index >= data.length)
		{
			throw new ArrayIndexOutOfBoundsException(
				type + " out of bounds: " + index + " >= " + data.length
			);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final T[][] data;
	private final Class<T> type;
	private final int rowCount, columnCount;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public Matrix(final T[][] data)
	{
		super();
		this.data = validateArray(data);
		this.type = (Class<T>)data.getClass().getComponentType().getComponentType();
		this.rowCount = data.length;
		this.columnCount = data[0].length;
	}

	@SuppressWarnings("unchecked")
	public Matrix(final Class<T> type, final int rows, final int columns)
	{
		super();
		this.type = type;
		this.data = (T[][])Array.newInstance(type, rows, columns);
		this.rowCount = rows;
		this.columnCount = columns;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<T> getType()
	{
		return this.type;
	}

	protected T[][] getData()
	{
		return this.data;
	}

	protected T[] getArray(final int arrayIndex)
	{
		return this.data[arrayIndex];
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(1024);
		final T[][] data = this.data;

		for(int x = 0; x < data.length; x++)
		{
			final T[] row = data[x];
			for(final T t : row)
			{
				sb.append(t).append('\t');
			}
			sb.append('\n');
		}
		return sb.toString();
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 *
	 * @param rowIndex
	 * @param colIndex
	 */
	public T getElementAt(final int rowIndex, final int colIndex)
	{
		return this.data[rowIndex][colIndex];
	}
	/**
	 *
	 * @param rowIndex
	 * @param colIndex
	 * @param newElement
	 * @return the old element at position (rowIndex, colIndex)
	 */
	public T setElementAt(final int rowIndex, final int colIndex, final T newElement)
	{
		synchronized(this.data)
		{
			final T oldElement = this.data[rowIndex][colIndex];
			this.data[rowIndex][colIndex] = newElement;
			return oldElement;
		}
	}
	/**
	 *
	 * @param rowIndex1
	 * @param colIndex1
	 * @param rowIndex2
	 * @param colIndex2
	 */
	public void swapElements(final int rowIndex1, final int colIndex1, final int rowIndex2, final int colIndex2)
	{
		synchronized(this.data)
		{
			final T element1 = this.data[rowIndex1][colIndex1];
			this.data[rowIndex1][colIndex1] = this.data[rowIndex2][colIndex2];
			this.data[rowIndex2][colIndex2] = element1;
		}
	}

	/**
	 *
	 */
	public Iterable<T> iterate()
	{
		return new MatrixIterable();
	}



	protected Iterable<T> iterate(
		final int firstRow, final int firstColumn, final int lastRow, final int lastColumn, final Double range
	)
		throws IllegalArgumentException
		{
		validateRowIndex(firstRow, this.data, "firstRow");
		validateRowIndex(lastRow, this.data, "lastRow");
		validateColumnIndex(firstColumn, this.data[firstRow], "firstColumn");
		validateColumnIndex(lastColumn, this.data[firstRow], "lastColumn");
		return new MatrixIterable(firstRow, lastRow, firstColumn, lastColumn, range);
		}
	/**
	 *
	 * @param firstRow
	 * @param firstColumn
	 * @param lastRow
	 * @param lastColumn
	 * @throws IllegalArgumentException
	 */
	public Iterable<T> iterate(final int firstRow, final int firstColumn, final int lastRow, final int lastColumn)
		throws IllegalArgumentException
	{
		return this.iterate(firstRow, firstColumn, lastRow, lastColumn, null);
	}

	public Iterable<T> iterate(final int centerRowIndex, final int centerColumnIndex, final int squareRadius)
		throws IllegalArgumentException
	{
		return this.iterate(
			Math.max(centerRowIndex    - squareRadius, 0),
			Math.max(centerColumnIndex - squareRadius, 0),
			Math.min(centerRowIndex    + squareRadius, this.rowCount),
			Math.min(centerColumnIndex + squareRadius, this.columnCount),
			null
		);
	}

	public Iterable<T> iterate(final int centerRowIndex, final int centerColumnIndex, final double range)
		throws IllegalArgumentException
	{
		final int squareRadius = (int)range;
		return this.iterate(
			Math.max(centerRowIndex    - squareRadius, 0),
			Math.max(centerColumnIndex - squareRadius, 0),
			Math.min(centerRowIndex    + squareRadius, this.rowCount),
			Math.min(centerColumnIndex + squareRadius, this.columnCount),
			range
		);
	}


	/**
	 *
	 * 
	 *
	 */
	private final class MatrixIterable implements Iterable<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int firstRow;
		final int lastRow;
		final int firstColumn;
		final int lastColumn;
		private final Double range;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		MatrixIterable()
		{
			this(0, Matrix.this.data.length - 1, 0, Matrix.this.data[0].length - 1, null);
		}

		MatrixIterable(
			final int firstRow,
			final int lastRow,
			final int firstColumn,
			final int lastColumn,
			final Double range
		)
		{
			super();
			this.firstRow = firstRow;
			this.lastRow = lastRow;
			this.firstColumn = firstColumn;
			this.lastColumn = lastColumn;
			this.range = range;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Iterator<T> iterator()
		{
			return new MatrixIterator(this.range);
		}



		/**
		 *
		 * 
		 *
		 */
		private class MatrixIterator implements Iterator<T>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private       T[]            currentArray   ;
			private       int            currentRowIndex = MatrixIterable.this.firstRow - 1;
			              int            currentColIndex = MatrixIterable.this.firstColumn;
			private final HasNextChecker hasNextChecker ;
			private final double         rangeSquare    ;

			private final int
				centerRowIndex = (MatrixIterable.this.lastRow    + MatrixIterable.this.firstRow   ) / 2,
			    centerColIndex = (MatrixIterable.this.lastColumn + MatrixIterable.this.firstColumn) / 2
			  ;

			{
				this.nextRow();
			}

			final boolean isInRange()
			{
				final int rowDif = this.currentRowIndex - this.centerRowIndex;
				final int colDif = this.currentColIndex - this.centerColIndex;
				return rowDif * rowDif + colDif * colDif <= this.rangeSquare;
			}


			/**
			 * @param hasNextChecker
			 */
			MatrixIterator(final Double range)
			{
				super();
				this.hasNextChecker = range != null
				? () ->
				{
					//FIXME range checking:
					//hasNext-checker interface
					/* if: currentCol < lastCol/2:
					 *     skip while (coordinates are out of range and currentColIndex++ <= RowIterable.this.lastColumn)
					 * else:
					 *     return true if coordinates are in range
					 */
					if(MatrixIterator.this.currentColIndex <= (MatrixIterable.this.lastColumn + 1) / 2)
					{
						while(!MatrixIterator.this.isInRange())
						{
							if(++MatrixIterator.this.currentColIndex > MatrixIterable.this.lastColumn)
							{
								return false;
							}
						}
						return true;
					}
					return this.isInRange();
				}
				: () ->
				{
					return this.currentColIndex <= MatrixIterable.this.lastColumn;
				};

				//only used by hasNextChecker
				this.rangeSquare = range != null ? range * range : 0;
			}


			/**
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext()
			{
				// (20.05.2010 TM)TODO: hasNext() user plugin Object
				if(this.hasNextChecker.hasNext())
				{
					return true;
				}

				while(this.nextRow())
				{
					if(this.hasNextChecker.hasNext())
					{
						return true;
					}
				}
				return false;
			}

			/**
			 * @see java.util.Iterator#next()
			 */
			@Override
			public T next()
			{
				// (20.05.2010 TM)TODO: next() user plugin Object
				try
				{
					return this.currentArray[this.currentColIndex++];
				}
				catch(final ArrayIndexOutOfBoundsException e)
				{
					throw new NoSuchElementException();
				}
			}

			/**
			 *
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove()
			{
				this.currentArray[this.currentColIndex] = null;
			}


			private boolean nextRow()
			{
				this.currentColIndex = MatrixIterable.this.firstColumn;
				final T[][] iterables = Matrix.this.data;
				int loopIndex = this.currentRowIndex;
				T[] loopIterable = null;
				while(loopIterable == null)
				{
					loopIndex++;
					if(loopIndex > MatrixIterable.this.lastRow)
					{
						return false;
					}
					loopIterable = iterables[loopIndex];
				}
				this.currentArray = loopIterable;
				this.currentRowIndex = loopIndex;

				return true;
			}

		}

	}

	private interface HasNextChecker
	{
		public boolean hasNext();
	}

	public enum IterationMode
	{
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

}
