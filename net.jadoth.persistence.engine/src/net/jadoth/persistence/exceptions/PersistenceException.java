package net.jadoth.persistence.exceptions;

import net.jadoth.exceptions.IndexBoundsException;

public class PersistenceException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceException()
	{
		this(null, null);
	}

	public PersistenceException(final String message)
	{
		this(message, null);
	}

	public PersistenceException(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceException(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final String message()
	{
		return super.getMessage();
	}

	public String assembleDetailString()
	{
		return null;
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = super.getMessage();
		return explicitMessage != null
			? " (" + explicitMessage + ")"
			: ""
		;
	}

	public String assembleOutputString()
	{
		// JDK concept or improved concept based on if assembleDetailString is overwritten
		final String detailString = this.assembleDetailString();
		return detailString == null
			? this.getMessage()
			: this.assembleDetailString() + this.assembleExplicitMessageAddon()
		;
	}

	/**
	 * Returns an assembled output String due to bad method design in {@link Throwable}.
	 * Albeit being named "getMessage" by the JDK developers, this method should be seen
	 * as "assembleOutputString" as this is its purpose.
	 * For the actual message getter, see {@link #message()}.
	 *
	 * @return this exception type's generic message plus an explicit message if present.
	 * @see IndexBoundsException
	 */
	@Override
	public String getMessage()
	{
		return this.assembleOutputString();
	}

}
