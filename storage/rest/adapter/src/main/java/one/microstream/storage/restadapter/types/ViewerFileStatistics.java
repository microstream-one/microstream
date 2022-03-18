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

import one.microstream.storage.types.StorageRawFileStatistics.FileStatistics;

/*
 * Simple POJO for easy JSON creation of one.microstream.viewer.ViewerFileStatistics
 */
public class ViewerFileStatistics extends ViewerStorageFileStatisticsItem
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	long fileNumber;
	String file;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerFileStatistics()
	{
		super();
	}

	public ViewerFileStatistics(
		final long fileCount,
		final long liveDataLength,
		final long totalDataLength,
		final long fileNumber,
		final String file)
	{
		super(fileCount, liveDataLength, totalDataLength);
		this.fileNumber = fileNumber;
		this.file = file;
	}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ViewerFileStatistics New(final FileStatistics src)
	{
		return new ViewerFileStatistics(
			src.fileCount(),
			src.liveDataLength(),
			src.totalDataLength(),
			src.fileNumber(),
			src.file());
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public long getFileNumber()
	{
		return this.fileNumber;
	}

	public void setFileNumber(final long fileNumber)
	{
		this.fileNumber = fileNumber;
	}

	public String getFile()
	{
		return this.file;
	}

	public void setFile(final String file)
	{
		this.file = file;
	}
}
