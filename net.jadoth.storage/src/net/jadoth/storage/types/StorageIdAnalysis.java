package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.collections.ConstHashTable;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.Swizzle.IdType;
import net.jadoth.typing.KeyValue;

public interface StorageIdAnalysis
{
	public XGettingTable<Swizzle.IdType, Long> highestIdsPerType();
	
	public XGettingEnum<Long> occuringTypeIds();


	public static StorageIdAnalysis Empty()
	{
		return new StorageIdAnalysis.Implementation(X.emptyTable(), null);
	}

	public static StorageIdAnalysis New(final Long highestTid, final Long highestOid, final Long highestCid)
	{
		return New(
			highestTid,
			highestOid,
			highestCid,
			null
		);
	}
	
	public static StorageIdAnalysis New(
		final Long               highestTid     ,
		final Long               highestOid     ,
		final Long               highestCid     ,
		final XGettingEnum<Long> occuringTypeIds
	)
	{
		return New(
			ConstHashTable.New(
				X.KeyValue(Swizzle.IdType.TID, highestTid),
				X.KeyValue(Swizzle.IdType.OID, highestOid),
				X.KeyValue(Swizzle.IdType.CID, highestCid)
			),
			occuringTypeIds
		);
	}

	public static StorageIdAnalysis New(
		final XGettingSequence<KeyValue<Swizzle.IdType, Long>> values         ,
		final XGettingEnum<Long>                               occuringTypeIds
	)
	{
		return new StorageIdAnalysis.Implementation(
			ConstHashTable.New(notNull(values)),
			occuringTypeIds == null
				? X.empty()
				: EqHashEnum.New(occuringTypeIds)
		);
	}

	public final class Implementation implements StorageIdAnalysis
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingTable<Swizzle.IdType, Long> highestIdsPerType;
		final XGettingEnum<Long>                  occuringTypeIds  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final XGettingTable<IdType, Long> highestIdsPerType,
			final XGettingEnum<Long>          occuringTypeIds
		)
		{
			super();
			this.highestIdsPerType = highestIdsPerType;
			this.occuringTypeIds   = occuringTypeIds  ;
		}

		@Override
		public final XGettingTable<IdType, Long> highestIdsPerType()
		{
			return this.highestIdsPerType;
		}
		
		@Override
		public final XGettingEnum<Long> occuringTypeIds()
		{
			return this.occuringTypeIds;
		}

	}

}
