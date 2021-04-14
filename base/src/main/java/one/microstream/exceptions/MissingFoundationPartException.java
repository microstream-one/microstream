package one.microstream.exceptions;


public class MissingFoundationPartException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> missingAssemblyPartType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
	// getters //
	////////////

	public Class<?> getMissingSssemblyPartType()
	{
		return this.missingAssemblyPartType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	public String assembleDetailString()
	{
		return this.missingAssemblyPartType != null
			? "Missing assembly part of type " + this.missingAssemblyPartType.toString() + ". "
			: ""
		;
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = super.getMessage();
		return explicitMessage == null ? "" : " (" + explicitMessage + ")";
	}

	public String assembleOutputString()
	{
		return this.assembleDetailString() + this.assembleExplicitMessageAddon();
	}
	
	/**
	 * Returns an assembled output String due to bad method design in {@link Throwable}.
	 *
	 * @return this exception type's generic string plus an explicit message if present.
	 */
	@Override
	public String getMessage() // intentionally not final to enable subclasses to change the behavior again
	{
		return this.assembleOutputString();
	}

}
