package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.FileNotFoundException;


public interface StorageEntityTypeExportFileProvider
{
	public StorageLockedFile provideExportFile(StorageEntityTypeHandler entityType);



	public final class Implementation implements StorageEntityTypeExportFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final File   directory ;
		private final String fileSuffix;

		private final transient String cachedFileSuffix;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final File directory, final String fileSuffix)
		{
			super();
			this.directory = notNull(directory);
			this.fileSuffix = fileSuffix;
			this.cachedFileSuffix = fileSuffix == null ? "" : '.' + fileSuffix;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		public final String fileSuffix()
		{
			return this.fileSuffix;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageLockedFile provideExportFile(final StorageEntityTypeHandler entityType)
		{
			// TypeId must be included since only that is the unique identifier of a type.
			final File file = new File(this.directory, entityType.typeName() + "_" + entityType.typeId() + this.cachedFileSuffix);
			try
			{
				return StorageLockedFile.openLockedFile(file);
			}
			catch(final FileNotFoundException e)
			{
				throw new RuntimeException(e); // (10.12.2014)EXCP: proper exception
			}

//			final VarString vs = VarString.New()
//			.padLeft(Long.toString(entityType.typeId()), XChars.maxCharCount_long() - 1, '0')
//			.add('_')
//			.add(entityType.typeName())
//			.add(this.cachedFileSuffix)
//			;
//			return new File(this.directory, vs.toString());
		}

	}

}
