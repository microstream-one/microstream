package one.microstream.persistence.binary.exceptions;

import one.microstream.chars.XChars;

public class BinaryPersistenceExceptionStateArrayLength extends BinaryPersistenceExceptionStateArray
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object[] actualArray ;
	private final int      passedLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength
	)
	{
		this(actualArray, passedLength, null, null);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final String message
	)
	{
		this(actualArray, passedLength, message, null);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final Throwable cause
	)
	{
		this(actualArray, passedLength, null, cause);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final String message, final Throwable cause
	)
	{
		this(actualArray, passedLength, message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.actualArray  = actualArray ;
		this.passedLength = passedLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object[] getActualArray()
	{
		return this.actualArray;
	}

	public int getPassedLength()
	{
		return this.passedLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Array length mismatch for array " + XChars.systemString(this.actualArray) + ": actual length = "
			+ this.actualArray.length + ", passed length = " + this.passedLength + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
