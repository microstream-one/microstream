package one.microstream.afs.sql.types;

import javax.sql.DataSource;

import one.microstream.afs.sql.types.SqlProviderHana.StoreType;
import one.microstream.configuration.types.Configuration;

public class SqlFileSystemCreatorHana extends SqlFileSystemCreator
{
	public SqlFileSystemCreatorHana()
	{
		super("hana");
	}
	
	@Override
	protected SqlProvider createSqlProvider(
		final Configuration sqlConfiguration,
		final DataSource    dataSource
	)
	{
		final StoreType storeType = sqlConfiguration.opt("store-type")
			.map(name -> StoreType.valueOf(name.toUpperCase()))
			.orElse(StoreType.ROW)
		;
		return SqlProviderHana.New(dataSource, storeType);
	}
	
}
