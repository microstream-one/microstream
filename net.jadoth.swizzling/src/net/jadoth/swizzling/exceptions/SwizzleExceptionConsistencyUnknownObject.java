package net.jadoth.swizzling.exceptions;

import net.jadoth.chars.XChars;



public class SwizzleExceptionConsistencyUnknownObject extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Object object;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyUnknownObject(final Object object)
	{
		this(object, null, null);
	}

	public SwizzleExceptionConsistencyUnknownObject(
		final Object object,
		final String message
	)
	{
		this(object, message, null);
	}

	public SwizzleExceptionConsistencyUnknownObject(
		final Object object,
		final Throwable cause
	)
	{
		this(object, null, cause);
	}

	public SwizzleExceptionConsistencyUnknownObject(
		final Object object,
		final String message, final Throwable cause
	)
	{
		this(object, message, cause, true, true);
	}

	public SwizzleExceptionConsistencyUnknownObject(
		final Object object,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.object = object ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public Object getObject()
	{
		return this.object;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Unknown object: " + XChars.systemString(this.object)
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
