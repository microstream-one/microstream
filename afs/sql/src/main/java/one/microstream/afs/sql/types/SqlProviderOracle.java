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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import one.microstream.chars.VarString;

public interface SqlProviderOracle extends SqlProvider
{
	public static SqlProviderOracle New(
		final DataSource dataSource
	)
	{
		return New(null, null, dataSource);
	}

	public static SqlProviderOracle New(
		final String     catalog   ,
		final String     schema    ,
		final DataSource dataSource
	)
	{
		return new Default(
			mayNull(catalog)   ,
			mayNull(schema)    ,
			notNull(dataSource)
		);
	}


	public static class Default extends SqlProvider.Abstract implements SqlProviderOracle
	{
		Default(
			final String     catalog   ,
			final String     schema    ,
			final DataSource dataSource
		)
		{
			super(
				catalog   ,
				schema    ,
				dataSource
			);
		}

		@Override
		public Iterable<String> createDirectoryQueries(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("create table ");
			this.addSqlTableName(vs, tableName);
			vs.add(" (");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(" varchar2(").add(IDENTIFIER_COLUMN_LENGTH).add(") not null, ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(" number(19) not null, ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(" number(19) not null, ");
			this.addSqlColumnName(vs, DATA_COLUMN_NAME);
			vs.add(" blob not null, constraint ");
			this.addNameQuoted(vs, tableName + "_pk");
			vs.add(" primary key (");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add("))");

			return Arrays.asList(vs.toString());
		}
		
		@Override
		public boolean queryDirectoryExists(
			final Connection connection,
			final String     tableName
		)
			throws SQLException
		{
			try(PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM user_tables WHERE table_name=?"
			))
			{
				statement.setString(1, tableName);
				try(ResultSet result = statement.executeQuery())
				{
					return result.next()
						? result.getLong(1) > 0L
						: false
					;
				}
			}
		}
		
		@Override
		public Set<String> queryDirectories(
			final Connection connection,
			final String     prefix
		)
			throws SQLException
		{
			final Set<String> directories = new HashSet<>();
			
			if(prefix != null)
			{
				try(PreparedStatement statement = connection.prepareStatement(
					"SELECT table_name FROM user_tables WHERE table_name LIKE ?"
				))
				{
					statement.setString(1, prefix);
					try(ResultSet result = statement.executeQuery())
					{
						while(result.next())
						{
							directories.add(result.getString(1));
						}
					}
				}
			}
			else
			{
				try(Statement statement = connection.createStatement())
				{
					try(ResultSet result = statement.executeQuery("SELECT table_name FROM user_tables"))
					{
						while(result.next())
						{
							directories.add(result.getString(1));
						}
					}
				}
			}
			
			return directories;
		}

	}

}
