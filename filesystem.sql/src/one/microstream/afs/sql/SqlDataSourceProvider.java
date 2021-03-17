package one.microstream.afs.sql;

import javax.sql.DataSource;

@FunctionalInterface
public interface SqlDataSourceProvider
{
	public DataSource provideDataSource();
}
