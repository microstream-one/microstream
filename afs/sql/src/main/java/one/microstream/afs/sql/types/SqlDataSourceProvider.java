package one.microstream.afs.sql.types;

import javax.sql.DataSource;

@FunctionalInterface
public interface SqlDataSourceProvider
{
	public DataSource provideDataSource();
}
