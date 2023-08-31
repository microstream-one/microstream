package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

import java.util.function.Consumer;

public interface StorageChannelImportBatch
{
	public long batchOffset();

	public long batchLength();

	public void iterateEntities(Consumer<? super StorageChannelImportEntity> iterator);

	public StorageChannelImportEntity first();
	
	
	/* to optimize the common case where a batch contains only one entity, the batch itself is the first entity.
	 * The additional memory is insignificant if there are many entities and few large batches.
	 * If there are many short batches, then memory is saved by incorporating the first entity.
	 */
	public static class Default
	extends StorageChannelImportEntity.Default
	implements StorageChannelImportBatch
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		long                              batchOffset;
		long                              batchLength;
		StorageChannelImportBatch.Default batchNext  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default()
		{
			super(0, 0, null);
		}

		Default(
			final long                      batchOffset ,
			final int                       entityLength,
			final long                      objectId    ,
			final StorageEntityType.Default type
		)
		{
			super(entityLength, objectId, type);
			this.batchOffset = batchOffset ;
			this.batchLength = entityLength;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public long batchOffset()
		{
			return this.batchOffset;
		}

		@Override
		public final long batchLength()
		{
			return this.batchLength;
		}

		@Override
		public final void iterateEntities(final Consumer<? super StorageChannelImportEntity> iterator)
		{
			for(StorageChannelImportEntity.Default e = this.first(); e != null; e = e.next)
			{
				iterator.accept(e);
			}
		}

		@Override
		public final StorageChannelImportEntity.Default first()
		{
			return this.type != null  ? this  : this.batchNext;
		}

		@Override
		public final String toString()
		{
			return "batch" + "[" + this.length + "]" + (this.batchNext == null ? "" : " " + this.batchNext.toString());
		}

	}
	
}
