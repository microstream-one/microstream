package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.ConstHashTable;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.Persistence.IdType;
import one.microstream.typing.KeyValue;

public interface StorageIdAnalysis
{
	public XGettingTable<Persistence.IdType, Long> highestIdsPerType();
	
	public XGettingEnum<Long> occuringTypeIds();


	public static StorageIdAnalysis Empty()
	{
		return new StorageIdAnalysis.Default(X.emptyTable(), null);
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
				X.KeyValue(Persistence.IdType.TID, highestTid),
				X.KeyValue(Persistence.IdType.OID, highestOid),
				X.KeyValue(Persistence.IdType.CID, highestCid)
			),
			occuringTypeIds
		);
	}

	public static StorageIdAnalysis New(
		final XGettingSequence<KeyValue<Persistence.IdType, Long>> values         ,
		final XGettingEnum<Long>                               occuringTypeIds
	)
	{
		return new StorageIdAnalysis.Default(
			ConstHashTable.New(notNull(values)),
			occuringTypeIds == null
				? X.empty()
				: EqHashEnum.New(occuringTypeIds)
		);
	}

	public final class Default implements StorageIdAnalysis
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingTable<Persistence.IdType, Long> highestIdsPerType;
		final XGettingEnum<Long>                      occuringTypeIds  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
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
