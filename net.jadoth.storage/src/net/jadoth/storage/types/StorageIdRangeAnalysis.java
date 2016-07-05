package net.jadoth.storage.types;

import static net.jadoth.Jadoth.keyValue;
import static net.jadoth.Jadoth.notNull;
import net.jadoth.collections.ConstHashTable;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.Swizzle.IdType;
import net.jadoth.util.KeyValue;

public interface StorageIdRangeAnalysis
{
	public XGettingTable<Swizzle.IdType, Long> highestIdsPerType();


	public static StorageIdRangeAnalysis Empty()
	{
		return new StorageIdRangeAnalysis.Implementation(X.emptyTable());
	}

	public static StorageIdRangeAnalysis New(final Long highestTid, final Long highestOid, final Long highestCid)
	{
		return new StorageIdRangeAnalysis.Implementation(
			ConstHashTable.New(
				keyValue(Swizzle.IdType.TID, highestTid),
				keyValue(Swizzle.IdType.OID, highestOid),
				keyValue(Swizzle.IdType.CID, highestCid)
			)
		);
	}

	public static StorageIdRangeAnalysis New(final XGettingSequence<KeyValue<Swizzle.IdType, Long>> values)
	{
		return new StorageIdRangeAnalysis.Implementation(ConstHashTable.New(notNull(values)));
	}

	public final class Implementation implements StorageIdRangeAnalysis
	{
		final XGettingTable<Swizzle.IdType, Long> highestIdsPerType;

		Implementation(final XGettingTable<IdType, Long> highestIdsPerType)
		{
			super();
			this.highestIdsPerType = highestIdsPerType;
		}

		@Override
		public final XGettingTable<IdType, Long> highestIdsPerType()
		{
			return this.highestIdsPerType;
		}

	}

}
