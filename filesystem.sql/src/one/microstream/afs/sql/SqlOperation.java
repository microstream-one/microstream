package one.microstream.afs.sql;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlOperation<T>
{
	public T execute(Connection connection) throws SQLException;
}