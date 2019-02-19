package net.jadoth.storage.types;

public interface StorageBackupProblemHandler
{
	/**
	 * Might throw an exception to stop the backup handling thread.
	 * 
	 * @param problemCount
	 * @param problemacity
	 */
	public void reportAllKindsOfPeskyProblems(long problemCount, double problemacity);
}
