package one.microstream.afs.sql;

@FunctionalInterface
public interface SqlPathVisitor
{
	public void visitItem(String itemName);
}
