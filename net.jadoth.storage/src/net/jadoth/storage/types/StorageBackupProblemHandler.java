package net.jadoth.storage.types;

import net.jadoth.storage.exceptions.StorageExceptionBackup;

public interface StorageBackupProblemHandler
{
	/**
	 * Throws an exception to stop the backup handling thread.
	 */
	public void handleException(RuntimeException exception) throws StorageExceptionBackup;
	
}
