package one.microstream.afs.sql.types;

/*-
 * #%L
 * MicroStream Abstract File System - SQL
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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.Arrays;

import javax.sql.DataSource;

import one.microstream.chars.VarString;

public interface SqlProviderHana extends SqlProvider
{
	public static enum StoreType
	{
		ROW, COLUMN
	}
	
	
	public static SqlProviderHana New(
		final DataSource dataSource,
		final StoreType  storeType
	)
	{
		return New(null, null, dataSource, storeType);
	}

	public static SqlProviderHana New(
		final String     catalog   ,
		final String     schema    ,
		final DataSource dataSource,
		final StoreType  storeType
	)
	{
		return new Default(
			mayNull(catalog)   ,
			mayNull(schema)    ,
			notNull(dataSource),
			notNull(storeType)
		);
	}


	public static class Default extends SqlProvider.Abstract implements SqlProviderHana
	{
		private final StoreType storeType;
		
		Default(
			final String     catalog   ,
			final String     schema    ,
			final DataSource dataSource,
			final StoreType  storeType
		)
		{
			super(
				catalog   ,
				schema    ,
				dataSource
			);
			this.storeType = storeType;
		}
		
		@Override
		public Iterable<String> createDirectoryQueries(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("create ").add(this.storeType.name().toLowerCase()).add(" table ");
			this.addSqlTableName(vs, tableName);
			vs.add(" (");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(" varchar(").add(IDENTIFIER_COLUMN_LENGTH).add(") not null, ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(" bigint not null, ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(" bigint not null, ");
			this.addSqlColumnName(vs, DATA_COLUMN_NAME);
			vs.add(" blob not null, primary key(");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add("))");

			return Arrays.asList(vs.toString());
		}

	}

}
