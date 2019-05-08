package one.microstream.storage.types;

public interface StorageObjectIdRangeEvaluator
{
	public void evaluateObjectIdRange(long lowestObjectId, long highestObjectId);


	public final class Default implements StorageObjectIdRangeEvaluator
	{
		@Override
		public void evaluateObjectIdRange(final long lowestObjectId, final long highestObjectId)
		{
			// no-op default implementation.
		}

	}
}
