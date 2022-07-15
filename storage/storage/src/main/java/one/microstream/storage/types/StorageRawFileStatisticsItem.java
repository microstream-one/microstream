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

public interface StorageRawFileStatisticsItem
{
	public long fileCount();

	public long liveDataLength();

	public long totalDataLength();



	public abstract class Abstract implements StorageRawFileStatisticsItem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long fileCount      ;
		final long liveDataLength ;
		final long totalDataLength;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Abstract(final long fileCount, final long liveDataLength, final long totalDataLength)
		{
			super();
			this.fileCount       = fileCount      ;
			this.liveDataLength  = liveDataLength ;
			this.totalDataLength = totalDataLength;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long fileCount()
		{
			return this.fileCount;
		}

		@Override
		public final long liveDataLength()
		{
			return this.liveDataLength;
		}

		@Override
		public final long totalDataLength()
		{
			return this.totalDataLength;
		}

	}

}
