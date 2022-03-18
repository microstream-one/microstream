package one.microstream.util.xcsv;

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

import static one.microstream.X.notNull;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XSequence;
import one.microstream.typing.XTypes;

public final class XCsvEntityCollector<T> implements XCsvRowCollector
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCsvEntityParser.Abstract<T> parser;
	private final XSequence<? super T>                      rows  ;
	private final BulkList<String>                          row   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public XCsvEntityCollector(
		final XCsvEntityParser.Abstract<T> parser,
		final XSequence<? super T>                      rows
	)
	{
		super();
		this.parser = notNull(parser) ;
		this.rows   = notNull(rows)   ;
		this.row    = new BulkList<>();
	}

	@Override
	public final void beginTable(
		final String                   tableName  ,
		final XGettingSequence<String> columnNames,
		final XGettingList<String>     columnTypes
	)
	{
		this.parser.beginTable(tableName, columnNames, columnTypes);
	}

	@Override
	public final void accept(final char[] data, final int offset, final int length)
	{
		this.row.add(data == null ? null : new String(data, offset, length));
	}

	@Override
	public void completeRow()
	{
		try
		{
			this.parser.validateRow(this.row);
		}
		catch(final Exception e)
		{
			throw new RuntimeException("Row validation failed at row " + this.rows.size(), e);
		}
		final T entity;
		try
		{
			entity = this.parser.apply(this.row);
		}
		catch(final RuntimeException e)
		{
			throw new RuntimeException("Exception while parsing row " + XTypes.to_int(this.rows.size()), e);
		}
		this.rows.add(entity);
		this.row.clear();
	}

	@Override
	public final void completeTable()
	{
		this.row.clear();
		this.parser.completeTable();
	}

	public final XSequence<? super T> rows()
	{
		return this.rows;
	}

}
