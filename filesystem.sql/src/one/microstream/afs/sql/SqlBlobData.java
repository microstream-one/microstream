package one.microstream.afs.sql;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlBlobData
{
	public void setAsParameter(PreparedStatement statement, int index) throws SQLException;


	public static SqlBlobData New(
		final Blob blob
	)
	{
		return (statement, index) -> statement.setBlob(index, blob);
	}

	public static SqlBlobData New(
		final byte[] blob
	)
	{
		return (statement, index) -> statement.setBytes(index, blob);
	}

}
