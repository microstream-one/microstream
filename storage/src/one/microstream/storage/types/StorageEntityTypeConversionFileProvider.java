package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;
import one.microstream.persistence.types.PersistenceTypeDefinition;


public interface StorageEntityTypeConversionFileProvider
{
	public AWritableFile provideConversionFile(PersistenceTypeDefinition typeDescription, AFile sourceFile);



	public final class Default implements StorageEntityTypeConversionFileProvider
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
			this.fileSuffix = fileSuffix        ;
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
		public AWritableFile provideConversionFile(
			final PersistenceTypeDefinition typeDescription,
			final AFile                     sourceFile
		)
		{
			// TypeId must be included since only that is the unique identifier of a type.
			
			final String fileName = typeDescription.typeName() + "_" + typeDescription.typeId();
			final AFile targetFile = this.directory.ensureFile(fileName, this.fileSuffix);
			
			return targetFile.useWriting();
		}

	}

}
