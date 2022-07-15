package one.microstream.chars;

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

import static one.microstream.math.XMath.notNegative;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;
import one.microstream.collections.ConstList;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableList;
import one.microstream.typing.XTypes;

public interface StringTable
{
	/**
	 * An arbitrary name identifying this table instance, potentially {@code null}.
	 *
	 * @return this table's name.
	 */
	public String name();

	public XGettingEnum<String> columnNames();

	public XGettingList<String> columnTypes();

	public XGettingList<String[]> rows();
	
	public <C extends BiConsumer<String, String>> C mapTo(
		C                          target     ,
		Function<String[], String> keyMapper  ,
		Function<String[], String> valueMapper
	);



	public interface Creator
	{
		public StringTable createStringTable(
			String                   name       ,
			XGettingSequence<String> columnNames,
			XGettingList<String>     columnTypes,
			XGettingList<String[]>   rows
		);
	}


	public final class Default implements StringTable
	{
		public static final class Creator implements StringTable.Creator
		{

			@Override
			public StringTable createStringTable(
				final String                   name       ,
				final XGettingSequence<String> columnNames,
				final XGettingList<String>     columnTypes,
				final XGettingList<String[]>   rows
			)
			{
				return new StringTable.Default(name, columnNames, columnTypes, rows);
			}

		}


		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static void validateColumnCount(final int columnCount, final XGettingList<String[]> rows)
		{
			final long columnCountMismatchIndex = rows.scan(new ColumnCountValidator(columnCount));
			if(columnCountMismatchIndex >= 0)
			{
				throw new IllegalArgumentException(
					"Invalid column count in row " + columnCountMismatchIndex
					+ " (" + columnCount + " required, " + rows.at(columnCountMismatchIndex).length + " available)"
				);
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                  name   ;
		private final EqConstHashEnum<String> columns;
		private final ConstList<String>       types  ;
		private final ConstList<String[]>     rows   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final XGettingSequence<String> columns    ,
			final XGettingList<String>     columnTypes,
			final XGettingList<String[]>   rows
		)
		{
			this(null, columns, columnTypes, rows);
		}

		public Default(
			final String                   name       ,
			final XGettingSequence<String> columns    ,
			final XGettingList<String>     columnTypes,
			final XGettingList<String[]>   rows
		)
		{
			super();
			this.name    = name                 ; // may be null
			this.columns = EqConstHashEnum.New(columns);
			validateColumnCount(XTypes.to_int(this.columns.size()), rows);
			this.types   = ConstList.New(columnTypes);
			this.rows    = ConstList.New(rows);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final XImmutableEnum<String> columnNames()
		{
			return this.columns;
		}

		@Override
		public final XGettingList<String> columnTypes()
		{
			return this.types;
		}

		@Override
		public final XImmutableList<String[]> rows()
		{
			return this.rows;
		}
		
		@Override
		public <C extends BiConsumer<String, String>> C mapTo(
			final C                          target     ,
			final Function<String[], String> keyMapper  ,
			final Function<String[], String> valueMapper
		)
		{
			for(final String[] row : this.rows)
			{
				target.accept(keyMapper.apply(row), valueMapper.apply(row));
			}

			return target;
		}


		static final class ColumnCountValidator implements Predicate<String[]>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final int columnCount;


			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			ColumnCountValidator(final int columnCount)
			{
				super();
				this.columnCount = notNegative(columnCount);
			}



			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final boolean test(final String[] row) throws ThrowBreak
			{
				return row.length != this.columnCount;
			}

		}

	}

}
