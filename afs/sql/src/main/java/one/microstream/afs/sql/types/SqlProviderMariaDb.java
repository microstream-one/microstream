package one.microstream.afs.sql.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import javax.sql.DataSource;

public interface SqlProviderMariaDb extends SqlProvider
{
	public static SqlProviderMariaDb New(
		final DataSource dataSource
	)
	{
		return New(null, null, dataSource);
	}

	public static SqlProviderMariaDb New(
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


	public static class Default extends SqlProviderMySql.Default implements SqlProviderMariaDb
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

	}

}
