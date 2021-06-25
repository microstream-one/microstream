package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
