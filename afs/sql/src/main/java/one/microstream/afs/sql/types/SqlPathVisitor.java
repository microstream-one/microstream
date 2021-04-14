package one.microstream.afs.sql.types;

@FunctionalInterface
public interface SqlPathVisitor
{
	public void visitItem(String itemName);
}
