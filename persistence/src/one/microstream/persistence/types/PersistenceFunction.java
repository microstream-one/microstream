package one.microstream.persistence.types;

public interface PersistenceFunction
{
	/**
	 * Applies any action on the passed instance (e.g.: simply looking up its object ID or
	 * storing its state to a storage medium) and returns the object ID that identifies the passed instance.
	 * The returned OID may be the existing one for the passed instance or a newly associated one.
	 *
	 * @param instance the instance to which the function shall be applied.
	 * @return the object ID (OID) that is associated with the passed instance.
	 */
	public <T> long apply(T instance);

}
