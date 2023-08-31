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

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.storage.exceptions.StorageException;


public interface StorageEntityTypeExportFileProvider
{
	public AWritableFile provideExportFile(StorageEntityTypeHandler entityType);

	
	
	public static String uniqueTypeFileNameSeparator()
	{
		return "_";
	}

	public static String toUniqueTypeFileName(final PersistenceTypeDefinition type)
	{
		return StorageEntityTypeExportFileProvider.toUniqueTypeFileName(type.typeName(), type.typeId());
	}
	
	public static String toUniqueTypeFileName(final String typeName, final long typeId)
	{
		// TypeId must be included since only that is the unique identifier of a type.
		return typeName + uniqueTypeFileNameSeparator() + typeId;
	}
	
	/* (20.02.2020 TM)XXX: abstract import/export filename logic
	 * These static methods are just a hotfix.
	 * The proper solution must be to introduce a StorageImportExportFileNameHandler or something like that
	 * that handles export file name creation and conversion typeId parsing.
	 * And then StorageDataConverterTypeCsvToBinary and StorageEntityTypeExportFileProvider must reference
	 * the SAME instance to have compatible logic.
	 */
	public static long getTypeIdFromUniqueTypeFileName(final String uniqueTypeFileName)
	{
		final int lastIndexOfSeparator = uniqueTypeFileName.lastIndexOf(uniqueTypeFileNameSeparator());
		if(lastIndexOfSeparator < 0)
		{
			throw new StorageException(
				"UniqueTypeFileNameSeparator '"
				+ uniqueTypeFileNameSeparator()
				+ "' was not found in file name \""
				+ uniqueTypeFileName + "\"."
			);
		}
		
		final String typeIdString = uniqueTypeFileName.substring(lastIndexOfSeparator + 1);
		try
		{
			return Long.parseLong(typeIdString);
		}
		catch(final NumberFormatException e)
		{
			throw new StorageException("Invalid TypeId String in file name \"" + uniqueTypeFileName + "\".", e);
		}
	}
	
	
	/**
	 * 
	 * @since 08.00.00
	 */
	public interface Defaults
	{
		/**
		 * Default file suffix for binary files.
		 * 
		 * @return {@code "bin"}
		 */
		public static String defaultFileSuffix()
		{
			return "bin";
		}
	}
	
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageEntityTypeExportFileProvider}.
	 * 
	 * @param directory target directory for the export files
	 * @return a new {@link StorageEntityTypeExportFileProvider}
	 * @since 08.00.00
	 */
	public static StorageEntityTypeExportFileProvider New(final ADirectory directory)
	{
		return New(
			directory,
			Defaults.defaultFileSuffix()
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageEntityTypeExportFileProvider}.
	 * 
	 * @param directory target directory for the export files
	 * @param fileSuffix suffix for created files
	 * @return a new {@link StorageEntityTypeExportFileProvider}
	 * @since 08.00.00
	 */
	public static StorageEntityTypeExportFileProvider New(final ADirectory directory, final String fileSuffix)
	{
		return new StorageEntityTypeExportFileProvider.Default(
			notNull(directory),
			notEmpty(fileSuffix)
		);
	}
	

	public final class Default implements StorageEntityTypeExportFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ADirectory directory ;
		private final String     fileSuffix;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final ADirectory directory, final String fileSuffix)
		{
			super();
			this.directory  = notNull(directory);
			this.fileSuffix = fileSuffix;
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final String fileSuffix()
		{
			return this.fileSuffix;
		}

		@Override
		public final AWritableFile provideExportFile(final StorageEntityTypeHandler entityType)
		{
			final String        name  = StorageEntityTypeExportFileProvider.toUniqueTypeFileName(entityType);
			final AFile         file  = this.directory.ensureFile(null, name, this.fileSuffix);
			final AWritableFile wFile = file.useWriting();
			
			return wFile;
		}

	}

}
