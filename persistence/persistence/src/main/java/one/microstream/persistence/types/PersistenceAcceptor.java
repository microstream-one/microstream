package one.microstream.persistence.types;

@FunctionalInterface
public interface PersistenceAcceptor
{
	public void accept(long objectId, Object instance);
	
	
	
	public static void noOp(final long objectId, final Object instance)
	{
		// no-op
	}
	
}