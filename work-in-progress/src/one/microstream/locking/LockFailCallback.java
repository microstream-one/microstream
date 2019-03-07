package one.microstream.locking;

public interface LockFailCallback<T, O>
{
	public void handleLockFail(T subject, O desiredOwner, O actualOwner);
}
