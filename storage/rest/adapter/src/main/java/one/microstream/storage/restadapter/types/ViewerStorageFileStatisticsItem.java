package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import one.microstream.storage.types.StorageRawFileStatisticsItem;

/*
 * Simple POJO for easy JSON creationone.microstream.viewer.ViewerStorageRawFileStatisticsItem
 */
public class ViewerStorageFileStatisticsItem
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	long fileCount;
	long liveDataLength;
	long totalDataLength;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerStorageFileStatisticsItem()
	{
		super();
	}

	public ViewerStorageFileStatisticsItem(
		final long fileCount,
		final long liveDataLength,
		final long totalDataLength)
	{
		super();
		this.fileCount = fileCount;
		this.liveDataLength = liveDataLength;
		this.totalDataLength = totalDataLength;
	}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ViewerStorageFileStatisticsItem New(final StorageRawFileStatisticsItem src)
	{
		return new ViewerStorageFileStatisticsItem(
			src.fileCount(),
			src.liveDataLength(),
			src.totalDataLength());
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public long getFileCount()
	{
		return this.fileCount;
	}

	public void setFileCount(final long fileCount)
	{
		this.fileCount = fileCount;
	}

	public long getLiveDataLength()
	{
		return this.liveDataLength;
	}

	public void setLiveDataLength(final long liveDataLength)
	{
		this.liveDataLength = liveDataLength;
	}

	public long getTotalDataLength()
	{
		return this.totalDataLength;
	}

	public void setTotalDataLength(final long totalDataLength)
	{
		this.totalDataLength = totalDataLength;
	}
}
