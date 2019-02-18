package net.jadoth.storage.types;

public interface StorageBackupProblemHandler
{
	// (18.02.2019 TM)FIXME: JET-55: serioulize
	/**
	 * Might throw an exception to stop the backup handling thread.
	 * 
	 * @param problemCount
	 * @param problemacity
	 */
	public void reportAllKindsOfPeskyProblems(long problemCount, double problemacity);
}
