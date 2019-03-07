package one.microstream.locking;

public interface LockOwnerTypeHolder<O>
{
	public Class<O> ownerType();
}
