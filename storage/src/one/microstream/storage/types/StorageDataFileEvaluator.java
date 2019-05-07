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
	
	public static StorageDataFileEvaluator New(final double dissolveRatio)
	{
		return New(
			Defaults.defaultFileMinimumSize(),
			Defaults.defaultFileMaximumSize(),
			dissolveRatio                    ,
			Defaults.defaultResolveHeadfile()
		);
	}

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

	public static StorageDataFileEvaluator New(
		final int    fileMinimumSize,
		final int    fileMaximumSize,
		final double dissolveRatio
	)
	{
		return New(
			fileMinimumSize                  ,
			fileMaximumSize                  ,
			dissolveRatio                    ,
			Defaults.defaultResolveHeadfile()
		);
	}

	public static StorageDataFileEvaluator New(
		final int     fileMinimumSize,
		final int     fileMaximumSize,
		final double  minimumUseRatio,
		final boolean cleanupHeadFile
	)
	{
		if(fileMaximumSize <= fileMinimumSize)
		{
			// (24.06.2014)EXCP: proper exception
			throw new IllegalArgumentException(
				"Nonsensical size limits: min file size = " + fileMinimumSize + ", max file size = " + fileMaximumSize
			);
		}
		return new Implementation(
			XMath.positive    (fileMinimumSize),
			XMath.positive    (fileMaximumSize),
			XMath.positiveMax1(minimumUseRatio),
			                   cleanupHeadFile
		);
	}
	
	
	public interface Defaults
	{
		public static int defaultFileMinimumSize()
		{
			// 1 MB in common byte magnitude
			return 1 * 1024 * 1024;
		}

		public static int defaultFileMaximumSize()
		{
			// 8 MB in common byte magnitude
			return 8 * 1024 * 1024;
		}
		
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


	public final class Implementation implements StorageDataFileEvaluator
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

		Implementation(
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
