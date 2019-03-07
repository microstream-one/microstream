package one.microstream.storage.exceptions;

public final class StorageExceptionInvalidFile extends StorageException
{
	private final long fileLength;

	public StorageExceptionInvalidFile(final long fileLength)
	{
		super();
		this.fileLength = fileLength;
	}

	public StorageExceptionInvalidFile(final long fileLength, final Throwable cause)
	{
		super(cause);
		this.fileLength = fileLength;
	}

	public final long fileLength()
	{
		return this.fileLength;
	}

}
