package one.microstream.afs.sql;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import one.microstream.X;
import one.microstream.chars.VarString;


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

	public Blob createBlob(Connection connection, InputStream inputStream, long length);

	public String fileSizeQuery(String tableName);

	public String readDataQuery(String tableName);

	public String readDataQueryWithLength(String tableName);

	public String readDataQueryWithOffset(String tableName);

	public String readDataQueryWithRange(String tableName);

	public String fileExistsQuery(String tableName);

	public Iterable<String> createDirectoryQueries(String tableName);

	public String deleteFileQuery(String tableName);

	public String writeDataQuery(String tableName);

	public String moveFileQuerySameParent(String tableName);

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

		protected abstract char quoteOpen();

		protected abstract char quoteClose();

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
			this.addSqlColumnName(vs, START_COLUMN_NAME);
			vs.add(" asc");

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
				return operation.execute(connection);
			}
			catch(final SQLException e)
			{
				// TODO: proper exception
				throw new RuntimeException(e);
			}
		}

		@Override
		public Blob createBlob(
			final Connection  connection ,
			final InputStream inputStream,
			final long        length
		)
		{
			try
			{
				final Blob blob = connection.createBlob();
				try(final OutputStream outputStream = blob.setBinaryStream(1L))
				{
					int          remaining = X.checkArrayRange(length);
					final byte[] buffer    = new byte[Math.min(remaining, 8096)];
					while(remaining > 0)
					{
						final int read = inputStream.read(
							buffer,
							0,
							Math.min(remaining, buffer.length)
						);
						outputStream.write(buffer, 0, read);
						remaining -= read;
					}
				}

				return blob;
			}
			catch(final SQLException | IOException e)
			{
				// TODO: proper exception
				throw new RuntimeException(e);
			}

		}

		@Override
		public String fileSizeQuery(
			final String tableName
		)
		{
			final VarString vs = VarString.New();

			vs.add("select max(");
			this.addSqlColumnName(vs, END_COLUMN_NAME);
			vs.add(") from ");
			this.addSqlTableName(vs, tableName);
			vs.add(" where ");
			this.addSqlColumnName(vs, IDENTIFIER_COLUMN_NAME);
			vs.add("=?");

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
			vs.add(", ");
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
