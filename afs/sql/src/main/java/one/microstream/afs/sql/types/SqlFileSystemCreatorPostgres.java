package one.microstream.afs.sql.types;

import javax.sql.DataSource;

import one.microstream.configuration.types.Configuration;

public class SqlFileSystemCreatorPostgres extends SqlFileSystemCreator
{
	public SqlFileSystemCreatorPostgres()
	{
		super("postgres");
	}
	
	@Override
	protected SqlProvider createSqlProvider(
		final Configuration sqlConfiguration,
		final DataSource    dataSource
	)
	{
		return SqlProviderPostgres.New(
			sqlConfiguration.get("catalog"),
			sqlConfiguration.get("schema") ,
			dataSource
		);
	}
	
}
