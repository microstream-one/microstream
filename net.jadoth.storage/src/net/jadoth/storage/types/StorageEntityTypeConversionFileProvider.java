package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.persistence.types.PersistenceTypeDefinition;


public interface StorageEntityTypeConversionFileProvider
{
	public File provideConversionFile(PersistenceTypeDefinition<?> typeDescription, File sourceFile);



	public final class Implementation implements StorageEntityTypeConversionFileProvider
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
		public File provideConversionFile(final PersistenceTypeDefinition<?> typeDescription, final File sourceFile)
		{
			// TypeId must be included since only that is the unique identifier of a type.
			return new File(this.directory, typeDescription.typeName() + "_" + typeDescription.typeId() + this.cachedFileSuffix);
		}

	}

}
