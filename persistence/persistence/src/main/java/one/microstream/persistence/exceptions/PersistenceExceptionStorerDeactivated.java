package one.microstream.persistence.exceptions;

import one.microstream.util.UtilStackTrace;

/**
 * This exception indicates that the storage is currently in a read only mode
 * that denies the usage of {@link one.microstream.persistence.types.PersistenceStorer} methods.
 * 
 */
@SuppressWarnings("serial")
public class PersistenceExceptionStorerDeactivated extends PersistenceException
{
	@Override
	public String getMessage()
	{
		return "PersistenceStorer is in read only mode. Calling the method '" + UtilStackTrace.getThrowingMethodName(this) + "' is not allowed!";
	}
}
