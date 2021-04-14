package one.microstream.storage.exceptions;

public class StorageExceptionBackupChannelIndex extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long channelIndex;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupChannelIndex(final long channelIndex)
	{
		super();
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(final long channelIndex, final String message)
	{
		super(message);
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(final long channelIndex, final Throwable cause)
	{
		super(cause);
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(
		final long      channelIndex,
		final String    message     ,
		final Throwable cause
	)
	{
		super(message, cause);
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(
		final long      channelIndex      ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.channelIndex = channelIndex;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final long channelIndex()
	{
		return this.channelIndex;
	}
	
}
