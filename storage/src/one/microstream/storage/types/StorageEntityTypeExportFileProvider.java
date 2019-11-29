package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.io.XIO;


public interface StorageEntityTypeExportFileProvider
{
	public StorageLockedFile provideExportFile(StorageEntityTypeHandler entityType);



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
				: '.' + fileSuffix
			;
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
			final Path file = XIO.Path(this.directory, entityType.typeName() + "_" + entityType.typeId() + this.cachedFileSuffix);
			return StorageLockedFile.openLockedFile(file);

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
