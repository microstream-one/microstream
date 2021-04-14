package one.microstream.afs.sql.types;

import javax.sql.DataSource;

import one.microstream.configuration.types.Configuration;

public class SqlFileSystemCreatorMariaDb extends SqlFileSystemCreator
{
	public SqlFileSystemCreatorMariaDb()
	{
		super("mariadb");
	}
	
	@Override
	protected SqlProvider createSqlProvider(
		final Configuration sqlConfiguration,
		final DataSource    dataSource
	)
	{
		return SqlProviderMariaDb.New(
			sqlConfiguration.get("catalog"),
			sqlConfiguration.get("schema") ,
			dataSource
		);
	}
	
}
