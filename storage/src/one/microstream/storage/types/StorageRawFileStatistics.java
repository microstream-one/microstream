package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import java.text.DecimalFormat;
import java.util.Date;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;

public interface StorageRawFileStatistics extends StorageRawFileStatisticsItem
{
	public Date creationTime();

	public int channelCount();

	public XGettingTable<Integer, ? extends ChannelStatistics> channelStatistics();

	

	public static StorageRawFileStatistics New(
		final Date                                                creationTime     ,
		final long                                                fileCount        ,
		final long                                                liveDataLength   ,
		final long                                                totalDataLength  ,
		final XGettingTable<Integer, ? extends ChannelStatistics> channelStatistics
	)
	{
		return new StorageRawFileStatistics.Default(
			    notNull(creationTime)     ,
			notNegative(fileCount)        ,
			notNegative(liveDataLength)   ,
			notNegative(totalDataLength)  ,
			    notNull(channelStatistics)
		);
	}

	public final class Default
	extends StorageRawFileStatisticsItem.Abstract
	implements StorageRawFileStatistics
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final Date creationTime;

		final XGettingTable<Integer, ? extends ChannelStatistics> channelStatistics;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Date creationTime   ,
			final long fileCount      ,
			final long liveDataLength ,
			final long totalDataLength,
			final XGettingTable<Integer, ? extends ChannelStatistics> channelStatistics
		)
		{
			super(fileCount, liveDataLength, totalDataLength);
			this.creationTime      = notNull(creationTime)     ;
			this.channelStatistics = notNull(channelStatistics);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final Date creationTime()
		{
			return this.creationTime;
		}

		@Override
		public final int channelCount()
		{
			return (int)this.channelStatistics.size();
		}

		@Override
		public final XGettingTable<Integer, ? extends ChannelStatistics> channelStatistics()
		{
			return this.channelStatistics;
		}

		private static double ratio(final long value1, final long value2)
		{
			return value2 == 0 ? 0 : (double)value1 / value2;
		}

		public final VarString assembleString(final VarString vs)
		{
			final DecimalFormat ratioFormat = new DecimalFormat("0.00%");

			vs
			.add("Storage Statistics " + this.creationTime()).lf()
			.tab().add("global file count:\t"        + this.fileCount      ).lf()
			.tab().add("global live data length:\t"  + this.liveDataLength ).lf()
			.tab().add("global total data length:\t" + this.totalDataLength).lf()
			.tab().add("global space efficiency:\t"  + ratioFormat.format(
				ratio(this.liveDataLength, this.totalDataLength))
			).lf()
			.tab().add("channel count:\t" + this.channelCount()).lf()
			;
			for(final ChannelStatistics cs : this.channelStatistics.values())
			{
				vs
				.lf()
				.add("Channel " + cs.channelIndex()).lf()
				.tab().add("file count:\t"        + cs.fileCount()      ).lf()
				.tab().add("live data length:\t"  + cs.liveDataLength() ).lf()
				.tab().add("total data length:\t" + cs.totalDataLength()).lf()
				.tab().add("space efficiency:\t"  + ratioFormat.format(
					ratio(cs.liveDataLength(), cs.totalDataLength()))
				).lf()
				;

				for(final FileStatistics fs : cs.files())
				{
					vs
					.tab().add(fs.file())
					.add(" (").add(fs.liveDataLength()).add(" / ").add(fs.totalDataLength())
					.add(", ").add(ratioFormat.format(ratio(fs.liveDataLength(), fs.totalDataLength())))
					.add(")").lf()
					;
				}
			}

			return vs;
		}

		@Override
		public final String toString()
		{
			return this.assembleString(VarString.New()).toString();
		}

	}



	public interface ChannelStatistics extends StorageRawFileStatisticsItem
	{
		public int channelIndex();

		public XGettingSequence<? extends FileStatistics> files();


		
		public static ChannelStatistics New(
			final int                                        channelIndex   ,
			final long                                       fileCount      ,
			final long                                       liveDataLength ,
			final long                                       totalDataLength,
			final XGettingSequence<? extends FileStatistics> files
		)
		{
			return new ChannelStatistics.Default(
				notNegative(channelIndex)   ,
				notNegative(fileCount)      ,
				notNegative(liveDataLength) ,
				notNegative(totalDataLength),
					notNull(files)
			);
		}

		public final class Default
		extends StorageRawFileStatisticsItem.Abstract
		implements ChannelStatistics
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			final int                                        channelIndex;
			final XGettingSequence<? extends FileStatistics> files       ;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default(
				final int                                        channelIndex   ,
				final long                                       fileCount      ,
				final long                                       liveDataLength ,
				final long                                       totalDataLength,
				final XGettingSequence<? extends FileStatistics> files
			)
			{
				super(fileCount, liveDataLength, totalDataLength);
				this.channelIndex = channelIndex;
				this.files        = files       ;
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public final int channelIndex()
			{
				return this.channelIndex;
			}

			@Override
			public final XGettingSequence<? extends FileStatistics> files()
			{
				return this.files;
			}

		}

	}



	public interface FileStatistics extends StorageRawFileStatisticsItem
	{
		public long fileNumber();

		public String file();


		
		public static FileStatistics New(
			final long   fileNumber     ,
			final String file           ,
			final long   liveDataLength ,
			final long   totalDataLength
		)
		{
			return new FileStatistics.Default(
				fileNumber     ,
				file           ,
				liveDataLength ,
				totalDataLength
			);
		}

		public final class Default
		extends StorageRawFileStatisticsItem.Abstract
		implements FileStatistics
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			final long   fileNumber;
			final String file      ;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default(
				final long   fileNumber     ,
				final String file           ,
				final long   liveDataLength ,
				final long   totalDataLength
			)
			{
				super(1, liveDataLength, totalDataLength);
				this.fileNumber = fileNumber;
				this.file       = file     ;
			}


			@Override
			public final long fileNumber()
			{
				return this.fileNumber;
			}

			@Override
			public final String file()
			{
				return this.file;
			}

		}

	}

}
