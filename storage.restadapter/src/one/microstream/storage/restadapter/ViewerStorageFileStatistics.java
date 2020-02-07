package one.microstream.storage.restadapter;

import java.util.Date;
import java.util.Hashtable;

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
	Hashtable<Integer, ViewerChannelStatistics> channelStatistics;

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
			final Hashtable<Integer, ViewerChannelStatistics> channelStatistics)
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
		final Hashtable<Integer, ViewerChannelStatistics> channelStatistics = new Hashtable<>();

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

	public Hashtable<Integer, ViewerChannelStatistics> getChannelStatistics()
	{
		return this.channelStatistics;
	}

	public void setChannelStatistics(final Hashtable<Integer, ViewerChannelStatistics> channelStatistics)
	{
		this.channelStatistics = channelStatistics;
	}
}
