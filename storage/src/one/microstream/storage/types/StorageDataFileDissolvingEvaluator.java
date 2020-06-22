package one.microstream.storage.types;


/**
 * Function type that evaluates if a storage file needs to be dissolved and its remaining data content be transferred
 * to a new file.
 * <p>
 * Note that any implementation of this type must be safe enough to never throw an exception as this would doom
 * the storage thread that executes it. Catching any exception would not prevent the problem for the channel thread
 * as the function has to work in order for the channel to work properly.
 * It is therefore strongly suggested that implementations only use "exception free" logic (like simple arithmetic)
 * or handle any possible exception internally.
 *
 * @author TM
 */
@FunctionalInterface
public interface StorageDataFileDissolvingEvaluator
{
	public boolean needsDissolving(StorageLiveDataFile storageFile);
}
