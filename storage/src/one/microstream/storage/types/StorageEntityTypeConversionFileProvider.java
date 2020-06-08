package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.io.XIO;
import one.microstream.persistence.types.PersistenceTypeDefinition;


public interface StorageEntityTypeConversionFileProvider
{
	public ZStorageLockedFile provideConversionFile(PersistenceTypeDefinition typeDescription, ZStorageFile sourceFile);



	public final class Default implements StorageEntityTypeConversionFileProvider
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
			this.fileSuffix       = fileSuffix        ;
			this.cachedFileSuffix = fileSuffix == null
				? ""
				: XIO.fileSuffixSeparator() + fileSuffix
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
		public ZStorageLockedFile provideConversionFile(
			final PersistenceTypeDefinition typeDescription,
			final ZStorageFile               sourceFile
		)
		{
			// TypeId must be included since only that is the unique identifier of a type.
			final Path file = XIO.Path(
				this.directory, typeDescription.typeName() + "_" + typeDescription.typeId() + this.cachedFileSuffix
			);
			XIO.unchecked.ensureDirectory(this.directory);
			
			return ZStorageLockedFile.openLockedFile(file);
		}

	}

}
