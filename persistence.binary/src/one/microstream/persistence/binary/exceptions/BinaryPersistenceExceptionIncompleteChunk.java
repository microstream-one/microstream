package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionIncompleteChunk extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long currentChunkLength;
	private final long totalChunkLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength)
	{
		this(currentChunkLength, totalChunkLength, null, null);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final String message
	)
	{
		this(currentChunkLength, totalChunkLength, message, null);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final Throwable cause
	)
	{
		this(currentChunkLength, totalChunkLength, null, cause);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final String message, final Throwable cause
	)
	{
		this(currentChunkLength, totalChunkLength, message, cause, true, true);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.currentChunkLength = currentChunkLength;
		this.totalChunkLength   = totalChunkLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getCurrentChunkLength()
	{
		return this.currentChunkLength;
	}

	public long getTotalChunkLength()
	{
		return this.totalChunkLength;
	}



}
