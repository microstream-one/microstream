package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.io.XIO;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.storage.exceptions.StorageException;


public interface StorageEntityTypeExportFileProvider
{
	public StorageLockedFile provideExportFile(StorageEntityTypeHandler entityType);

	
	
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
			// (20.02.2020 TM)EXCP: proper exception
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
	

	public final class Default implements StorageEntityTypeExportFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Path   directory ;
		private final String fileSuffix;

		private final transient String cachedFileSuffix;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final Path directory, final String fileSuffix)
		{
			super();
			this.directory        = notNull(directory);
			this.fileSuffix       = fileSuffix;
			this.cachedFileSuffix = fileSuffix == null
				? ""
				: XIO.fileSuffixSeparator() + fileSuffix
			;
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final String fileSuffix()
		{
			return this.fileSuffix;
		}

		@Override
		public final StorageLockedFile provideExportFile(final StorageEntityTypeHandler entityType)
		{
			final String fileName = StorageEntityTypeExportFileProvider.toUniqueTypeFileName(entityType);
			final Path   file     = XIO.Path(this.directory, fileName + this.cachedFileSuffix);
			
			return StorageLockedFile.openLockedFile(file);
		}

	}

}
