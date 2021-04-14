package one.microstream.afs.sql.types;

import javax.sql.DataSource;

import one.microstream.configuration.types.Configuration;

public class SqlFileSystemCreatorOracle extends SqlFileSystemCreator
{
	public SqlFileSystemCreatorOracle()
	{
		super("oracle");
	}
	
	@Override
	protected SqlProvider createSqlProvider(
		final Configuration sqlConfiguration,
		final DataSource    dataSource
	)
	{
		return SqlProviderOracle.New(
			sqlConfiguration.get("catalog"),
			sqlConfiguration.get("schema") ,
			dataSource
		);
	}
	
}
