package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import java.util.Date;
import java.util.HashMap;

import one.microstream.storage.types.StorageRawFileStatistics;

/*
 * Simple POJO for easy JSON creation of one.microstream.viewer.ViewerStorageRawFileStatistics
 */
public class ViewerStorageFileStatistics extends ViewerStorageFileStatisticsItem
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	Date creationTime;
	HashMap<Integer, ViewerChannelStatistics> channelStatistics;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerStorageFileStatistics()
	{
		super();
	}

	public ViewerStorageFileStatistics(
			final Date creationTime,
			final long fileCount,
			final long liveDataLength,
			final long totalDataLength,
			final HashMap<Integer, ViewerChannelStatistics> channelStatistics)
		{
			super(fileCount, liveDataLength, totalDataLength);
			this.creationTime = creationTime;
			this.channelStatistics = channelStatistics;
		}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ViewerStorageFileStatistics New(final StorageRawFileStatistics src)
	{
		final HashMap<Integer, ViewerChannelStatistics> channelStatistics = new HashMap<>();

		src.channelStatistics().forEach(e -> channelStatistics.put(e.key(), ViewerChannelStatistics.New(e.value())));

		return new ViewerStorageFileStatistics(
			src.creationTime(),
			src.fileCount(),
			src.liveDataLength(),
			src.totalDataLength(),
			channelStatistics);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Date getCreationTime()
	{
		return this.creationTime;
	}

	public void setCreationTime(final Date creationTime)
	{
		this.creationTime = creationTime;
	}

	public HashMap<Integer, ViewerChannelStatistics> getChannelStatistics()
	{
		return this.channelStatistics;
	}

	public void setChannelStatistics(final HashMap<Integer, ViewerChannelStatistics> channelStatistics)
	{
		this.channelStatistics = channelStatistics;
	}
}
