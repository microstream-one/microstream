package one.microstream.storage.restadapter.types;

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
