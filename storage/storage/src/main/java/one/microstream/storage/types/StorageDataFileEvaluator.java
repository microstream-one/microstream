package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.chars.VarString;


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
 */
public interface StorageDataFileEvaluator extends StorageDataFileDissolvingEvaluator
{
	@Override
	public boolean needsDissolving(StorageLiveDataFile storageFile);

	public boolean needsRetirement(long fileTotalLength);

	public int fileMinimumSize();

	public int fileMaximumSize();

	public int transactionFileMaximumSize();

	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.<p>
	 *
	 * @return a new {@link StorageDataFileEvaluator} instance.
	 *
	 * @see StorageDataFileEvaluator#New(int, int)
	 * @see StorageDataFileEvaluator#New(double)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New()
	{
		/*
		 * Validates its own default values, but the cost is negligible and it is a
		 * good defense against accidentally erroneous changes of the default values.
		 */
		return New(
			Defaults.defaultFileMinimumSize()          ,
			Defaults.defaultFileMaximumSize()          ,
			Defaults.defaultMinimumUseRatio()          ,
			Defaults.defaultResolveHeadfile()          ,
			Defaults.defaultTransactionFileMaxiumSize()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed value and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.<p>
	 *
	 * @param minimumUseRatio the ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent
	 *        the file from being dissolved. "Gap" data is anything that is not the latest version of an entity's data,
	 *        including older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 *        as a negative value length header).<br>
	 *        The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 *        file dissolving (data transfers to new files) is required and vice versa.
	 *
	 * @return a new {@link StorageDataFileEvaluator} instance.
	 *
	 * @see StorageDataFileEvaluator#New()
	 * @see StorageDataFileEvaluator#New(int, int)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static StorageDataFileEvaluator New(final double minimumUseRatio)
	{
		return New(
			Defaults.defaultFileMinimumSize()          ,
			Defaults.defaultFileMaximumSize()          ,
			minimumUseRatio                            ,
			Defaults.defaultResolveHeadfile()          ,
			Defaults.defaultTransactionFileMaxiumSize()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.<p>
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
	 * @return a new {@link StorageDataFileEvaluator} instance.
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
			fileMinimumSize                            ,
			fileMaximumSize                            ,
			Defaults.defaultMinimumUseRatio()          ,
			Defaults.defaultResolveHeadfile()          ,
			Defaults.defaultTransactionFileMaxiumSize()
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
	 * The parameters defined here give an opportunity to configure how "aggressive" this clean up shall be performed.
	 * The trade-off is between acceptable gap sizes and the required performance and disk-writes to clean them up.<br>
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
	 *        including older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
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
			fileMinimumSize                            ,
			fileMaximumSize                            ,
			minimumUseRatio                            ,
			Defaults.defaultResolveHeadfile()          ,
			Defaults.defaultTransactionFileMaxiumSize()
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.
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
	 *        including older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 *        as a negative value length header).<br>
	 *        The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 *        file dissolving (data transfers to new files) is required and vice versa.
	 * 
	 * @param cleanUpHeadFile a flag defining wether the current head file (the only file actively written to)
	 *        shall be subjected to file cleanups as well.
	 * 
	 * @return a new {@link StorageDataFileEvaluator} instance.
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
		return New(
			fileMinimumSize                            ,
			fileMaximumSize                            ,
			minimumUseRatio                            ,
			cleanUpHeadFile                            ,
			Defaults.defaultTransactionFileMaxiumSize()
			);
	}


	/**
	 * Pseudo-constructor method to create a new {@link StorageDataFileEvaluator} instance
	 * using the passed values and default values specified by {@link StorageDataFileEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageDataFileEvaluator#New(int, int, double)}.
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
	 *        including older versions of an entity and "comment" bytes (a sequence of bytes beginning with its length
	 *        as a negative value length header).<br>
	 *        The closer this value is to 1.0 (100%), the less disk space is occupied by storage files, but the more
	 *        file dissolving (data transfers to new files) is required and vice versa.
	 * 
	 * @param cleanUpHeadFile a flag defining wether the current head file (the only file actively written to)
	 *        shall be subjected to file cleanups as well.
	 * 
	 * @param transactionFileMaximumSize the maximum file size for transaction files. Lager files will
	 *        be deleted and a new one will be created. <br>
	 *        Note that the max file size should be smaller than the technical limit of 2 GB (Integer.MAX_VALUE)
	 *        to allow more appends without exceeding the limit until housekeeping will create a new one.
	 *
	 * @return a new {@link StorageDataFileEvaluator} instance.
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
		final boolean cleanUpHeadFile,
		final int     transactionFileMaximumSize
	)
	{
		Validation.validateParameters(fileMinimumSize, fileMaximumSize, minimumUseRatio, transactionFileMaximumSize);
		return new Default(fileMinimumSize, fileMaximumSize, minimumUseRatio, cleanUpHeadFile, transactionFileMaximumSize);
	}



	public interface Validation
	{
		public static int minimumFileSize()
		{
			return 1024;
		}

		public static int maximumFileSize()
		{
			return Integer.MAX_VALUE;
		}
		
		public static int maximumTransactionFileSize()
		{
			return 1024*1024*1024;
		}

		/**
		 * How much the maximum file size must be above the minimum file size.
		 *
		 * @return the minimum file size range
		 */
		public static int minimumFileSizeRange()
		{
			return 1024;
		}

		public static double useRatioLowerBound()
		{
			return 0.0;
		}

		public static double useRatioMaximum()
		{
			return 1.0;
		}

		public static void validateParameters(
			final int    fileMinimumSize,
			final int    fileMaximumSize,
			final double minimumUseRatio,
			final int    transactionFileMaximumSize
		)
		{
			// > maximumFileSize() can technically never happen for now, but the max value might change.
			if(fileMinimumSize < minimumFileSize() || fileMinimumSize > maximumFileSize())
			{
				throw new IllegalArgumentException(
					"Specified file minimum size of " + fileMinimumSize
					+ " is not in the valid range of ["
					+ minimumFileSize() + ", " + maximumFileSize() + "]."
				);
			}

			// > maximumFileSize() can technically never happen for now, but the max value might change.
			if(fileMaximumSize < minimumFileSize() || fileMaximumSize > maximumFileSize())
			{
				throw new IllegalArgumentException(
					"Specified file maximum size of " + fileMaximumSize
					+ " is not in the valid range of ["
					+ minimumFileSize() + ", " + maximumFileSize() + "]."
				);
			}

			if(fileMaximumSize - minimumFileSizeRange() < fileMinimumSize)
			{
				throw new IllegalArgumentException(
					"For the specified file minimum size of " + fileMinimumSize
					+ ", the specified file maximum size must at least be " + minimumFileSizeRange()
					+ " higher (" +  (fileMinimumSize + minimumFileSizeRange()) + " in total), but it is only "
					+ fileMaximumSize + "."
				);
			}

			if(minimumUseRatio <= useRatioLowerBound() || minimumUseRatio > useRatioMaximum())
			{
				throw new IllegalArgumentException(
					"Specified minimum usage ratio of "
					+ minimumUseRatio + " is not in the valid range of ]"
					+ useRatioLowerBound() + ", " + useRatioMaximum() + "]."
				);
			}
			
			// > maximumFileSize() can technically never happen for now, but the max value might change.
			if(transactionFileMaximumSize < minimumFileSize() || transactionFileMaximumSize > maximumTransactionFileSize())
			{
				throw new IllegalArgumentException(
					"Specified transaction file maximum size of " + transactionFileMaximumSize
					+ " is not in the valid range of ["
					+ minimumFileSize() + ", " + maximumFileSize() + "]."
				);
			}
		}
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

		public static int defaultTransactionFileMaxiumSize()
		{
			// 100 MB as default
			return 1024 * 1024 * 100;
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
		private final int     transactionFileMaximumSize;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final int     fileMinimumSize,
			final int     fileMaximumSize,
			final double  minimumUseRatio,
			final boolean cleanupHeadFile,
			final int     transactionFileMaximumSize
		)
		{
			super();
			this.fileMinimumSize            = fileMinimumSize;
			this.fileMaximumSize            = fileMaximumSize;
			this.minimumUseRatio            = minimumUseRatio;
			this.cleanupHeadFile            = cleanupHeadFile;
			this.transactionFileMaximumSize = transactionFileMaximumSize;
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
		
		public double minimumUseRatio()
		{
			return this.minimumUseRatio;
		}
		
		public boolean cleanupHeadFile()
		{
			return this.cleanupHeadFile;
		}
		
		@Override
		public final int transactionFileMaximumSize()
		{
			return this.transactionFileMaximumSize;
		}

		@Override
		public final boolean needsDissolving(final StorageLiveDataFile storageFile)
		{
			/*
			 * Dissolve file if one of three extreme cases (too small, too much gaps/overhead, too big) apply,
			 * with the over-sized case accounting for the special case of one single over-sized entity file to prevent
			 * constant dissolving of such files.
			 * Also, an undersized head file may never be dissolved as this would just create a new head file
			 * of the same size that would again be dissolved (looping forever).
			 *
			 * This logic means that every over-sized file gets dissolved into pieces until only normal case files
			 * and special case single-over-sized-entity files are left.
			 */

			/*
			 * Normally never dissolve head file as this might cause infinite head file migration
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

		private boolean isBelowMinimumSize(final StorageLiveDataFile storageFile)
		{
			return storageFile.totalLength() < this.fileMinimumSize();
		}

		private boolean hasTooMuchGapSpace(final StorageLiveDataFile storageFile)
		{
			return storageFile.dataFillRatio() < this.minimumUseRatio;
		}

		private boolean isAboveMaximumSize(final StorageLiveDataFile storageFile)
		{
			return storageFile.totalLength() > this.fileMaximumSize();
		}

		private boolean isGaplessSingleEntityFile(final StorageLiveDataFile storageFile)
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
