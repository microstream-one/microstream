package one.microstream.afs.sql.types;

import javax.sql.DataSource;

import one.microstream.configuration.types.Configuration;

public class SqlFileSystemCreatorSqlite extends SqlFileSystemCreator
{
	public SqlFileSystemCreatorSqlite()
	{
		super("sqlite");
	}
	
	@Override
	protected SqlProvider createSqlProvider(
		final Configuration sqlConfiguration,
		final DataSource    dataSource
	)
	{
		return SqlProviderSqlite.New(
			sqlConfiguration.get("catalog"),
			sqlConfiguration.get("schema") ,
			dataSource
		);
	}
	
}
