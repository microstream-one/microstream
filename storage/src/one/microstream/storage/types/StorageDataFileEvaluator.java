package one.microstream.storage.types;

import one.microstream.chars.VarString;
import one.microstream.math.XMath;


/**
 * Function type that evaluates if a storage file needs to be dissolved and its remaining data content be transferred
 * to a new file or if the current head storage file needs to retire and be replaced by a new one.
 * <p>
 * Note that any implementation of this type must be safe enough to never throw an exception as this would doom
 * the storage thread that executes it. Catching any exception would not prevent the problem for the channel thread
 * as the function has to work in order for the channel to work properly.
 * It is therefore strongly suggested that implementations only use "exception free" logic (like simple arithmetic)
 * or handle any possible exception internally.
 *
 * @author TM
 */
public interface StorageDataFileEvaluator extends StorageDataFileDissolvingEvaluator
{
	@Override
	public boolean needsDissolving(StorageDataFile<?> storageFile);

	public boolean needsRetirement(long fileTotalLength);

	public int fileMinimumSize();

	public int fileMaximumSize();


	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.<p>
	 * 
	 * @return {@linkDoc StorageDataFileEvaluator#New(int, int, double)@return}
	 * 
	 * @see StorageDataFileEvaluator#New(int, int)
	 * @see StorageDataFileEvaluator#New(double)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New()
	{
		/*
		 * Validates its own default values, but the cost is neglible and it is a
		 * good defense against accidentally erroneous changes of the default values.
		 */
		return New(
			Defaults.defaultFileMinimumSize(),
			Defaults.defaultFileMaximumSize(),
			Defaults.defaultMinimumUseRatio(),
			Defaults.defaultResolveHeadfile()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed value and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.<p>
	 * 
	 * @param minimumUseRatio {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * 
	 * @return {@linkDoc StorageDataFileEvaluator#New(int, int, double)@return}
	 * 
	 * @see StorageDataFileEvaluator#New()
	 * @see StorageDataFileEvaluator#New(int, int)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New(final double dissolveRatio)
	{
		return New(
			Defaults.defaultFileMinimumSize(),
			Defaults.defaultFileMaximumSize(),
			dissolveRatio                    ,
			Defaults.defaultResolveHeadfile()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.<p>
	 * 
	 * @param fileMinimumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * @param fileMaximumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * 
	 * @return {@linkDoc StorageDataFileEvaluator#New(int, int, double)@return}
	 * 
	 * @see StorageDataFileEvaluator#New()
	 * @see StorageDataFileEvaluator#New(double)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New(
		final int fileMinimumSize,
		final int fileMaximumSize
	)
	{
		return New(
			fileMinimumSize                  ,
			fileMaximumSize                  ,
			Defaults.defaultMinimumUseRatio(),
			Defaults.defaultResolveHeadfile()
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values.
	 * <p>
	 * A {@link StorageDataFileEvaluator} is used to evaluate storage data files regarding if they should be
	 * desolved, meaning copy all their still needed data to the current head file and then delete the file.<br>
	 * This technique is used to optimize (or "clean up") the occupied storage space: No longer needed data, e.g.
	 * of entities that became unreachable or prior versions of a still reachable entity, is considered a logical "gap",
	 * that occupies storage space unnecessarily. These "gaps" have to be removed in order to only occupy as much
	 * storage space as required.
	 * <p>
	 * The parameters defined here give an opportunity to configure how "aggressive" this clean up shall be performaned.
	 * The trade-off is between acceptale gap sizes and the required performance and disk-writes to clean them up.<br>
	 * Without specifying them, default values will be used, defined in {@link StorageDataFileEvaluator.Defaults}.
	 * 
	 * @param fileMinimumSize the minimum file size in bytes that a single storage file must have. Smaller files
	 *        will be dissolved.
	 * 
	 * @param fileMaximumSize the maximum file size in bytes that a single storage file may have. Larger files
	 *        will be dissolved.<br>
	 *        Note that a file can exceed this limit if it contains a single entity that already exceeds the limit.
	 *        E.g. an int array with 10 million elements would be about 40 MB in size and would exceed a file size
	 *        limit of, for example, 30 MB.
	 * 
	 * @param minimumUseRatio the ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent
	 *        the file from being dissolved. "Gap" data is anything that is not the latest version of an entity's data,
	 *        inluding older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 *        as a negative value length header).<br>
	 *        The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 *        file dissolving (data transfers to new files) is required and vice versa.
	 * 
	 * @return a new {@link StorageDataFileEvaluator} instance.
	 * 
	 * @see StorageDataFileEvaluator#New()
	 * @see StorageDataFileEvaluator#New(double)
	 * @see StorageDataFileEvaluator#New(int, int)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New(
		final int    fileMinimumSize,
		final int    fileMaximumSize,
		final double minimumUseRatio
	)
	{
		return New(
			fileMinimumSize                  ,
			fileMaximumSize                  ,
			minimumUseRatio                  ,
			Defaults.defaultResolveHeadfile()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.
	 * 
	 * @param fileMinimumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * @param fileMaximumSize {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * @param minimumUseRatio {@linkDoc StorageDataFileEvaluator#New(int, int, double):}
	 * @param cleanUpHeadFile a flag defining wether the current head file (the only file actively written to)
	 *        shall be subjected to file cleanups as well.
	 * 
	 * @return {@linkDoc StorageDataFileEvaluator#New(int, int, double)@return}
	 * 
	 * @see StorageDataFileEvaluator#New()
	 * @see StorageDataFileEvaluator#New(double)
	 * @see StorageDataFileEvaluator#New(int, int)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New(
		final int     fileMinimumSize,
		final int     fileMaximumSize,
		final double  minimumUseRatio,
		final boolean cleanUpHeadFile
	)
	{
		if(fileMaximumSize <= fileMinimumSize)
		{
			// (24.06.2014)EXCP: proper exception
			throw new IllegalArgumentException(
				"Nonsensical size limits: min file size = " + fileMinimumSize + ", max file size = " + fileMaximumSize
			);
		}
		return new Default(
			XMath.positive    (fileMinimumSize),
			XMath.positive    (fileMaximumSize),
			XMath.positiveMax1(minimumUseRatio),
			                   cleanUpHeadFile
		);
	}
	
	
	public interface Defaults
	{
		/**
		 * @return {@code 1 * 1024 * 1024} (meaning 1 MB minimum file size).
		 */
		public static int defaultFileMinimumSize()
		{
			// 1 MB in common byte magnitude
			return 1 * 1024 * 1024;
		}

		/**
		 * @return {@code 8 * 1024 * 1024} (meaning 8 MB maximum file size).
		 */
		public static int defaultFileMaximumSize()
		{
			// 8 MB in common byte magnitude
			return 8 * 1024 * 1024;
		}

		/**
		 * @return {@code 0.75} (meaning 75% minimum use ratio required).
		 */
		public static double defaultMinimumUseRatio()
		{
			// 75% non-gap ("useful") data.
			return 0.75;
		}
		
		public static boolean defaultResolveHeadfile()
		{
			return true;
		}
	}


	public final class Default implements StorageDataFileEvaluator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int     fileMinimumSize;
		private final int     fileMaximumSize;
		private final double  minimumUseRatio;
		private final boolean cleanupHeadFile;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final int     fileMinimumSize,
			final int     fileMaximumSize,
			final double  minimumUseRatio,
			final boolean cleanupHeadFile
		)
		{
			super();
			this.fileMinimumSize = fileMinimumSize;
			this.fileMaximumSize = fileMaximumSize;
			this.minimumUseRatio = minimumUseRatio;
			this.cleanupHeadFile = cleanupHeadFile;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int fileMinimumSize()
		{
			return this.fileMinimumSize;
		}

		@Override
		public final int fileMaximumSize()
		{
			return this.fileMaximumSize;
		}

		@Override
		public final boolean needsDissolving(final StorageDataFile<?> storageFile)
		{
			/*
			 * Dissolve file if one of three extreme cases (too small, too much gaps/overhead, too big) apply,
			 * with the oversized case accounting for the special case of one single oversized entity file to prevent
			 * constant dissolving of such files.
			 * Also, an undersized head file may never be dissolved as this would just create a new head file
			 * of the same size that would again be dissolved (looping forever).
			 *
			 * This logic means that every oversized file gets dissolved into pieces until only normal case files
			 * and special case single-oversized-entity files are left.
			 */
//			DEBUGStorage.println("Checking " + storageFile);

			/*
			 * Normally never dissovle head file as this might cause infinite head file migration
			 * However, specialized implementations (e.g. only checking gap space for manual one-shot consolidation)
			 * might decide to clean up head files as well.
			 */
			if(!this.cleanupHeadFile && storageFile.isHeadFile())
			{
				return false;
			}

			return this.isBelowMinimumSize(storageFile) && !storageFile.isHeadFile()
				|| this.hasTooMuchGapSpace(storageFile)
				|| this.isAboveMaximumSize(storageFile) && !this.isGaplessSingleEntityFile(storageFile)
			;
		}

		private boolean isBelowMinimumSize(final StorageDataFile<?> storageFile)
		{
			return storageFile.totalLength() < this.fileMinimumSize();
		}

		private boolean hasTooMuchGapSpace(final StorageDataFile<?> storageFile)
		{
			return storageFile.dataFillRatio() < this.minimumUseRatio;
		}

		private boolean isAboveMaximumSize(final StorageDataFile<?> storageFile)
		{
			return storageFile.totalLength() > this.fileMaximumSize();
		}

		private boolean isGaplessSingleEntityFile(final StorageDataFile<?> storageFile)
		{
			// file has only one entity and contains no further gaps
			return storageFile.hasSingleEntity() && storageFile.dataLength() == storageFile.totalLength();
		}

		@Override
		public final boolean needsRetirement(final long fileTotalLength)
		{
			return fileTotalLength >= this.fileMaximumSize;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("fileMinimumSize").tab().add('=').blank().add(this.fileMinimumSize).lf()
				.blank().add("fileMaximumSize").tab().add('=').blank().add(this.fileMaximumSize).lf()
				.blank().add("minimumUseRatio").tab().add('=').blank().add(this.minimumUseRatio).lf()
				.blank().add("cleanupHeadFile").tab().add('=').blank().add(this.cleanupHeadFile)
				.toString()
			;
		}

	}

}
