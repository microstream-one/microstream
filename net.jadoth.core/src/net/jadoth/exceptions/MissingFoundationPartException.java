package net.jadoth.exceptions;


public class MissingFoundationPartException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final Class<?> missingAssemblyPartType;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType
	)
	{
		this(missingSssemblyPartType, null, null);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final String message
	)
	{
		this(missingSssemblyPartType, message, null);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final Throwable cause
	)
	{
		this(missingSssemblyPartType, null, cause);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final String message, final Throwable cause
	)
	{
		this(missingSssemblyPartType, message, cause, true, true);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.missingAssemblyPartType = missingSssemblyPartType;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public Class<?> getMissingSssemblyPartType()
	{
		return this.missingAssemblyPartType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return (this.missingAssemblyPartType != null
			? "Missing assembly part of type " + this.missingAssemblyPartType.toString() + ". "
			: "")
		+ super.getMessage();
	}



}
