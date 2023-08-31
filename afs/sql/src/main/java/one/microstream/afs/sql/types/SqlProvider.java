package one.microstream.afs.sql.types;

/*-
 * #%L
 * microstream-afs-sql
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

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import one.microstream.afs.exceptions.AfsException;
import one.microstream.chars.VarString;
import one.microstream.exceptions.IORuntimeException;


public interface SqlProvider
{
	public final static String IDENTIFIER_COLUMN_NAME   = "identifier" ;
	public final static int    IDENTIFIER_COLUMN_TYPE   = Types.VARCHAR;
	public final static int    IDENTIFIER_COLUMN_LENGTH = 255          ;

	public final static String START_COLUMN_NAME        = "start"      ;
	public final static int    START_COLUMN_TYPE        = Types.BIGINT ;

	public final static String END_COLUMN_NAME          = "end"        ;
	public final static int    END_COLUMN_TYPE          = Types.BIGINT ;

	public final static String DATA_COLUMN_NAME         = "data"       ;
	public final static int    DATA_COLUMN_TYPE         = Types.BLOB   ;



	public String catalog();

	public String schema();

	public <T> T execute(SqlOperation<T> operation);

	public void setBlob(PreparedStatement statement, int index, InputStream inputStream, long length) throws SQLException;
	
	public boolean queryDirectoryExists(Connection connection, String tableName) throws SQLException;
	
	public Set<String> queryDirectories(Connection connection, String prefix) throws SQLException;

	/**
	 * <pre>
	 * select count(*), max('end')
	 * from [tableName]
	 * where 'identifier' = ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String fileSizeQuery(String tableName);

	/**
	 * <pre>
	 * select distinct 'identifier'
	 * from [tableName]
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String listFilesQuery(String tableName);

	/**
	 * <pre>
	 * select count(*)
	 * from [tableName]
	 * </pre>
	 *
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String countFilesQuery(String tableName);

	/**
	 * <pre>
	 * select 'start', 'end'
	 * from [tableName]
	 * where 'identifier' = ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String readMetadataQuery(String tableName);

	/**
	 * <pre>
	 * select 'start', 'end'
	 * from [tableName]
	 * where 'identifier' = ?
	 * and start &lt;= ? and end &gt;= ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String readMetadataQuerySingleSegment(String tableName);

	/**
	 * <pre>
	 * select *
	 * from [tableName]
	 * where 'identifier' = ?
	 * order by 'end' desc
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String readDataQuery(String tableName);

	/**
	 * <pre>
	 * select *
	 * from [tableName]
	 * where 'identifier' = ?
	 * and 'start' &lt; ?
	 * order by 'end' desc
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String readDataQueryWithLength(String tableName);

	/**
	 * <pre>
	 * select *
	 * from [tableName]
	 * where 'identifier' = ?
	 * and 'end' &gt;= ?
	 * order by 'end' desc
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String readDataQueryWithOffset(String tableName);

	/**
	 * <pre>
	 * select *
	 * from [tableName]
	 * where 'identifier' = ?
	 * and 'end' &gt;= ?
	 * and 'start' &lt;= ?
	 * order by 'end' desc
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String readDataQueryWithRange(String tableName);

	/**
	 * <pre>
	 * select count(*)
	 * from [tableName]
	 * where 'identifier' = ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String fileExistsQuery(String tableName);

	/**
	 * <pre>
	 * create table [tableName] (
	 * 'identifier' varchar(IDENTIFIER_COLUMN_LENGTH) not null,
	 * 'start' bigint not null,
	 * 'end' bigint not null,
	 * 'blob' not null,
	 * primary key ('identifier', 'start')
	 * )
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public Iterable<String> createDirectoryQueries(String tableName);

	/**
	 * <pre>
	 * delete from [tableName]
	 * where 'identifier' = ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String deleteFileQuery(String tableName);

	/**
	 * <pre>
	 * delete from [tableName]
	 * where 'identifier' = ?
	 * and 'start' &gt;= ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String deleteFileQueryFromStart(String tableName);

	/**
	 * <pre>
	 * delete from [tableName]
	 * where 'identifier' = ?
	 * and 'end' &gt;= ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String deleteFileQueryFromEnd(String tableName);

	/**
	 * <pre>
	 * insert into [tableName]
	 * ('identifier', 'start', 'end', 'data')
	 * values (?, ?, ?, ?)
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String writeDataQuery(String tableName);

	/**
	 * <pre>
	 * update [tableName]
	 * set 'identifier' = ?
	 * where 'identifier' = ?
	 * </pre>
	 * 
	 * @param tableName the table to query
	 * @return the native query string
	 */
	public String moveFileQuerySameParent(String tableName);

	/**
	 * <pre>
	 * insert into [targetTableName]
	 * ('identifier', 'start', 'end', 'data')
	 * select ?, 'start', 'end', 'data'
	 * from [sourceTableName]
	 * where 'identifier' = ?
	 * </pre>
	 * 
	 * @param sourceTableName the source table name to query
	 * @param targetTableName the target table name to query
	 * @return the native query string
	 */
	public String copyFileQuery(String sourceTableName, String targetTableName);



	public static abstract class Abstract implements SqlProvider
	{
		private final String     catalog   ;
		private final String     schema    ;
		private final DataSource dataSource;

		protected Abstract(
			final String     catalog   ,
			final String     schema    ,
			final DataSource dataSource
		)
		{
			this.catalog    = mayNull(catalog)   ;
			this.schema     = mayNull(schema)    ;
			this.dataSource = notNull(dataSource);
		}

		protected char quoteOpen()
		{
			return '"';
		}

		protected char quoteClose()
		{
			return '"';
		}

		protected VarString addSqlTableName(
			final VarString vs       ,
			final String    tableName
		)
		{
			return this.addNameQuoted(vs, tableName);
		}

		protected VarString addSqlColumnName(
			final VarString vs        ,
			final String    columnName
		)
		{
			return this.addNameQuoted(vs, columnName);
		}

		protected VarString addNameQuoted(
			final VarString vs  ,
			final String    name
		)
		{
			return vs
				.add(this.quoteOpen())
				.add(name)
				.add(this.quoteClose())
			;
		}

		protected String internalReadDataQuery(
			final String       tableName ,
			final VarString... conditions
		)
		{
			final VarString vs = VarString.New();

			vs.add("select * from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			for(final VarString condition : conditions)
			{
				vs.add(" and ").add(condition);
			}

			vs.add(" order by ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(" desc");

			return vs.toString();
		}

		@Override
		public String catalog()
		{
			return this.catalog;
		}

		@Override
		public String schema()
		{
			return this.schema;
		}

		@Override
		public <T> T execute(
			final SqlOperation<T> operation
		)
		{
			try(final Connection connection = this.dataSource.getConnection())
			{
				connection.setAutoCommit(false);
				
				try
				{
					final T result = operation.execute(connection);
					
					connection.commit();
					
					return result;
				}
				catch(final SQLException e)
				{
					connection.rollback();
					throw e;
				}
			}
			catch(final SQLException e)
			{
				throw new AfsException(e);
			}
		}

		@Override
		public void setBlob(
			final PreparedStatement statement  ,
			final int               index      ,
			final InputStream       inputStream,
			final long              length
		)
			throws SQLException
		{
			try
			{
				statement.setBinaryStream(index, inputStream, length);
			}
			catch(final SQLFeatureNotSupportedException featureNotSupported)
			{
				try
				{
					final byte[] bytes = new byte[checkArrayRange(length)];
					int offset = 0;
					while(offset < bytes.length - 1)
					{
						offset += inputStream.read(bytes, offset, bytes.length - offset);
					}
					statement.setBytes(index, bytes);
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}
			}
		}
		
		@Override
		public boolean queryDirectoryExists(
			final Connection connection,
			final String     tableName
		)
			throws SQLException
		{
			try(final ResultSet result = connection.getMetaData().getTables(
				this.catalog(),
				this.schema(),
				tableName,
				new String[] {"TABLE"}
			))
			{
				return result.next();
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
			
			try(final ResultSet result = connection.getMetaData().getTables(
				this.catalog(),
				this.schema(),
				prefix,
				new String[] {"TABLE"}
			))
			{
				while(result.next())
				{
					directories.add(result.getString("TABLE_NAME"));
				}
			}
			
			return directories;
		}

		@Override
		public String fileSizeQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select count(*), max(");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(") from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			return vs.toString();
		}

		@Override
		public String listFilesQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select distinct ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(" from ");
			this.addSqlTableName(vs, tableName);

			return vs.toString();
		}
		
		@Override
		public String countFilesQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select count(*) from ");
			this.addSqlTableName(vs, tableName);

			return vs.toString();
		}

		@Override
		public String readMetadataQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(" from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			return vs.toString();
		}

		@Override
		public String readMetadataQuerySingleSegment(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(" from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=? and ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add("<=? and ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(">=?");

			return vs.toString();
		}

		@Override
		public String readDataQuery(
			final String tableName
		)
		{
			return this.internalReadDataQuery(tableName);
		}

		@Override
		public String readDataQueryWithLength(
			final String tableName
		)
		{
			return this.internalReadDataQuery(
				tableName,
				this.addSqlColumnName(VarString.New(), START_COLUMN_NAME).add("<?")
			);
		}

		@Override
		public String readDataQueryWithOffset(
			final String tableName
		)
		{
			return this.internalReadDataQuery(
				tableName,
				this.addSqlColumnName(VarString.New(), END_COLUMN_NAME).add(">=?")
			);
		}

		@Override
		public String readDataQueryWithRange(
			final String tableName
		)
		{
			return this.internalReadDataQuery(
				tableName,
				this.addSqlColumnName(VarString.New(), END_COLUMN_NAME).add(">=?"),
				this.addSqlColumnName(VarString.New(), START_COLUMN_NAME).add("<=?")
			);
		}

		@Override
		public String fileExistsQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select count(*) from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			return vs.toString();
		}

		@Override
		public String deleteFileQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("delete from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			return vs.toString();
		}

		@Override
		public String deleteFileQueryFromStart(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("delete from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=? and ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(">=?");

			return vs.toString();
		}

		@Override
		public String deleteFileQueryFromEnd(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("delete from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=? and ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(">=?");

			return vs.toString();
		}

		@Override
		public String writeDataQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("insert into ");
			this.addSqlTableName(vs, tableName);
			vs.add(" (");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, DATA_COLUMN_NAME);
			vs.add(") values (?, ?, ?, ?)");

			return vs.toString();
		}

		@Override
		public String moveFileQuerySameParent(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("update ");
			this.addSqlTableName(vs, tableName);
			vs.add(" set ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=? where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			return vs.toString();
		}

		@Override
		public String copyFileQuery(
			final String sourceTableName,
			final String targetTableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("insert into ");
			this.addSqlTableName(vs, targetTableName);
			vs.add(" (");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, DATA_COLUMN_NAME);
			vs.add(") select ?, ");
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(", ");
			this.addSqlColumnName(vs, DATA_COLUMN_NAME);
			vs.add(" from ");
			this.addSqlTableName(vs, sourceTableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

			return vs.toString();
		}

	}

}
