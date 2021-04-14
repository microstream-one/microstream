package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionInvalidChunkLength extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long chunkLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionInvalidChunkLength(final long chunkLength)
	{
		this(chunkLength, null, null);
	}

	public BinaryPersistenceExceptionInvalidChunkLength(final long chunkLength,
		final String message
	)
	{
		this(chunkLength, message, null);
	}

	public BinaryPersistenceExceptionInvalidChunkLength(final long chunkLength,
		final Throwable cause
	)
	{
		this(chunkLength, null, cause);
	}

	public BinaryPersistenceExceptionInvalidChunkLength(final long chunkLength,
		final String message, final Throwable cause
	)
	{
		this(chunkLength, message, cause, true, true);
	}

	public BinaryPersistenceExceptionInvalidChunkLength(final long chunkLength,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.chunkLength = chunkLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getChunkLength()
	{
		return this.chunkLength;
	}



}
