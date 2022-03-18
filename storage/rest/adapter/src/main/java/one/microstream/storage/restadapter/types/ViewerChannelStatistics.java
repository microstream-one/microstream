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

import java.util.ArrayList;
import java.util.List;

import one.microstream.storage.types.StorageRawFileStatistics.ChannelStatistics;

/*
 * Simple POJO for easy JSON creation of one.microstream.viewer.ViewerChannelStatistics
 */
public class ViewerChannelStatistics extends ViewerStorageFileStatisticsItem
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	int channelIndex;
	List<ViewerFileStatistics> files;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerChannelStatistics()
	{
		super();
	}

	public ViewerChannelStatistics(
			final int channelIndex,
			final long fileCount,
			final long liveDataLength,
			final long totalDataLength,
			final List<ViewerFileStatistics> files
		)
		{
			super(fileCount, liveDataLength, totalDataLength);
			this.channelIndex = channelIndex;
			this.files = files;
		}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ViewerChannelStatistics New(final ChannelStatistics src)
	{
		final List<ViewerFileStatistics > files = new ArrayList<>();

		src.files().forEach( f-> files.add(ViewerFileStatistics.New(f)));

		return new ViewerChannelStatistics(
			src.channelIndex(),
			src.fileCount(),
			src.liveDataLength(),
			src.totalDataLength(),
			files);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public int getChannelIndex()
	{
		return this.channelIndex;
	}

	public void setChannelIndex(final int channelIndex)
	{
		this.channelIndex = channelIndex;
	}

	public List<ViewerFileStatistics> getFiles()
	{
		return this.files;
	}

	public void setFiles(final List<ViewerFileStatistics> files)
	{
		this.files = files;
	}
}
