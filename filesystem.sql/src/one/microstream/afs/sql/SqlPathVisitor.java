package one.microstream.afs.sql;

public interface SqlPathVisitor
{
	public void visitDirectory(SqlPath parent, String directoryName);

	public void visitFile(SqlPath parent, String fileName);
}
