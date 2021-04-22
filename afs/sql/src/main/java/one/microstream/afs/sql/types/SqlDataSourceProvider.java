package one.microstream.afs.sql.types;

import javax.sql.DataSource;

import one.microstream.configuration.types.Configuration;

@FunctionalInterface
public interface SqlDataSourceProvider
{
	public DataSource provideDataSource(Configuration configuration);
}
