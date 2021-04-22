package one.microstream.afs.sql.types;

import static one.microstream.chars.XChars.notEmpty;

import javax.sql.DataSource;

import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public abstract class SqlFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	private final String name;

	protected SqlFileSystemCreator(
		final String name
	)
	{
		super(AFileSystem.class);
		this.name = notEmpty(name);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final String        configurationKey = "sql." + this.name;
		final Configuration sqlConfiguration = configuration.child(configurationKey);
		if(sqlConfiguration == null)
		{
			return null;
		}
		
		final String dataSourceProviderClassName = sqlConfiguration.get("data-source-provider");
		if(dataSourceProviderClassName == null)
		{
			throw new ConfigurationException(
				sqlConfiguration,
				configurationKey + ".data-source-provider must be set"
			);
		}
		try
		{
			final SqlDataSourceProvider dataSourceProvider = (SqlDataSourceProvider)
				Class.forName(dataSourceProviderClassName).newInstance()
			;
			final SqlProvider sqlProvider = this.createSqlProvider(
				sqlConfiguration,
				dataSourceProvider.provideDataSource(sqlConfiguration.detach())
			);
			final boolean cache = configuration.optBoolean("cache").orElse(true);
			return SqlFileSystem.New(cache
				? SqlConnector.Caching(sqlProvider)
				: SqlConnector.New(sqlProvider)
			);
		}
		catch(InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			throw new ConfigurationException(sqlConfiguration, e);
		}
	}
	
	protected abstract SqlProvider createSqlProvider(
		Configuration sqlConfiguration,
		DataSource    dataSource
	);
	
}
