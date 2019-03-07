package net.jadoth.locking;

public interface LockOwnerTypeHolder<O>
{
	public Class<O> ownerType();
}
