package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyEnum extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public PersistenceExceptionTypeConsistencyEnum(final String constantName, final String enumClassName, final int ordinal, final long targetOrdinal)
	{
		super(
			buildMessage(
				constantName,
				enumClassName,
				ordinal,
				targetOrdinal
		));
	}
	
	private static String buildMessage(final String constantName, final String enumClassName, final int ordinal, final long targetOrdinal)
	{
		return "The ordinal of the enum constant " + constantName + " of " +
			enumClassName +
			"\nwould be change by the legacy type mapping from " +
			ordinal + " to " + targetOrdinal + ". This may cause the storage becoming corrupted." +
			"\nIf the ordinal change is intended you need to define a manual legacy type mapping!";
	}
}
	
