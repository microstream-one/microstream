package one.microstream.afs.sql.types;

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
