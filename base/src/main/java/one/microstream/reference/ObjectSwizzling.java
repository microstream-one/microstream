package one.microstream.reference;

public interface ObjectSwizzling
{
	/**
	 * Retrieves the instance associated with the passed {@literal objectId}. Retrieving means guaranteeing that
	 * the associated instance is returned. If it does not yet exist, it will be created from persisted data,
	 * including all non-lazily referenced objects it is connected to.
	 * 
	 * @param objectId the {@literal objectId} defining which instance to return.
	 * 
	 * @return the instance associated with the passed {@literal objectId}.
	 */
	public Object getObject(long objectId);
}
