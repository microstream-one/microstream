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
			/* don't bother with including a type id.
			 * TypeId mapping is the type dictionary's concern, not that of an export file.
			 * Also it messes up sorting files by name.
			 */
			return new File(this.directory, typeDescription.typeName() + this.cachedFileSuffix);

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
