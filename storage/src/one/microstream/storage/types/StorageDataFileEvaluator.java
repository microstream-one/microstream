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

	public int minimumFileSize();

	public int maximumFileSize();


	public static StorageDataFileEvaluator New(final int minFileSize, final int maxFileSize, final double dissolveRatio)
	{
		return New(minFileSize, maxFileSize, dissolveRatio, true);
	}

	public static StorageDataFileEvaluator New(
		final int     minFileSize    ,
		final int     maxFileSize    ,
		final double  dissolveRatio  ,
		final boolean cleanupHeadFile
	)
	{
		if(maxFileSize <= minFileSize)
		{
			// (24.06.2014)EXCP: proper exception
			throw new IllegalArgumentException("nonsensical size limits: min file size = " + minFileSize + ", max file size = " + maxFileSize);
		}
		return new Implementation(
			XMath.positive(minFileSize)  ,
			XMath.positive(maxFileSize)  ,
			XMath.positive(dissolveRatio),
			cleanupHeadFile
		);
	}


	public final class Implementation implements StorageDataFileEvaluator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int     minimumFileSize ;
		private final int     maximumFileSize ;
		private final double  minimumFillRatio;
		private final boolean cleanupHeadFile ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final int     minFileSize     ,
			final int     maxFileSize     ,
			final double  minimumFillRatio,
			final boolean cleanupHeadFile
		)
		{
			super();
			this.minimumFileSize  = minFileSize     ;
			this.maximumFileSize  = maxFileSize     ;
			this.minimumFillRatio = minimumFillRatio;
			this.cleanupHeadFile  = cleanupHeadFile ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int minimumFileSize()
		{
			return this.minimumFileSize;
		}

		@Override
		public final int maximumFileSize()
		{
			return this.maximumFileSize;
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
			return storageFile.totalLength() < this.minimumFileSize();
		}

		private boolean hasTooMuchGapSpace(final StorageDataFile<?> storageFile)
		{
			return storageFile.dataFillRatio() < this.minimumFillRatio;
		}

		private boolean isAboveMaximumSize(final StorageDataFile<?> storageFile)
		{
			return storageFile.totalLength() > this.maximumFileSize();
		}

		private boolean isGaplessSingleEntityFile(final StorageDataFile<?> storageFile)
		{
			// file has only one entity and contains no further gaps
			return storageFile.hasSingleEntity() && storageFile.dataLength() == storageFile.totalLength();
		}

		@Override
		public final boolean needsRetirement(final long fileTotalLength)
		{
			return fileTotalLength >= this.maximumFileSize;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("minFileSize"  ).tab().add('=').blank().add(this.minimumFileSize  ).lf()
				.blank().add("maxFileSize"  ).tab().add('=').blank().add(this.maximumFileSize  ).lf()
				.blank().add("dissolveRatio").tab().add('=').blank().add(this.minimumFillRatio)
				.toString()
			;
		}

	}

}
