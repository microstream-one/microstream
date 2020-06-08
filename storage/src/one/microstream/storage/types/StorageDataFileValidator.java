package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.storage.exceptions.StorageException;


public interface StorageDataFileValidator
{
	public void validateFile(
		ZStorageNumberedFile file           ,
		long                fileOffset     ,
		long                iterationLength
	);
	
	public default void validateFile(final ZStorageNumberedFile file)
	{
		this.validateFile(file, 0, file.length());
	}
		
	public default void freeMemory()
	{
		// no-op by default
	}
	
	
	public static StorageDataFileValidator New(
		final BinaryEntityRawDataIterator   entityDataIterator ,
		final StorageEntityDataValidator    entityDataValidator,
		final StorageFileEntityDataIterator fileIterator
	)
	{
		return new StorageDataFileValidator.Default(
			notNull(entityDataIterator),
			notNull(entityDataValidator),
			notNull(fileIterator)
		);
	}
	
	@Deprecated
	public static StorageDataFileValidator DebugLogging(
		final BinaryEntityRawDataIterator   entityDataIterator ,
		final StorageEntityDataValidator    entityDataValidator,
		final StorageFileEntityDataIterator fileIterator
	)
	{
		return new StorageDataFileValidator.Default(
			notNull(entityDataIterator),
			notNull(entityDataValidator),
			notNull(fileIterator)
		){
			@Override
			public void validateFile(final ZStorageNumberedFile file, final long fileOffset, final long iterationLength)
			{
				if(!Storage.isDataFile(file))
				{
					return;
				}
				
				XDebug.println("Validating file " + file.identifier() + "[" + fileOffset + ";" + (fileOffset + iterationLength) + "[");
				super.validateFile(file, fileOffset, iterationLength);
			}
		};
	}
	
	public class Default implements StorageDataFileValidator
	{
		///////////////////////////////
		// instance fields //
		////////////////////

		private final BinaryEntityRawDataIterator   entityDataIterator ;
		private final StorageEntityDataValidator    entityDataValidator;
		private final StorageFileEntityDataIterator fileIterator       ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final BinaryEntityRawDataIterator   entityDataIterator ,
			final StorageEntityDataValidator    entityDataValidator,
			final StorageFileEntityDataIterator fileIterator
		)
		{
			super();
			this.entityDataIterator  = notNull(entityDataIterator) ;
			this.entityDataValidator = notNull(entityDataValidator);
			this.fileIterator        = notNull(fileIterator)       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void freeMemory()
		{
			this.fileIterator.removeBuffer();
		}

		@Override
		public void validateFile(
			final ZStorageNumberedFile file           ,
			final long                fileOffset     ,
			final long                iterationLength
		)
		{
			if(!Storage.isDataFile(file))
			{
				return;
			}
			
			final long remainingLength = this.fileIterator.iterateEntityData(
				file,
				fileOffset,
				iterationLength,
				this.entityDataIterator,
				this.entityDataValidator
			);
			if(remainingLength != 0)
			{
				 // (01.03.2019 TM)EXCP: proper exception
				throw new StorageException(
					"Uncompletable validation: "
					+ remainingLength + " remaining bytes of " + iterationLength + " total cannot be validated"
					+ " in file " + file.identifier() + " at offset " + fileOffset
				);
			}
		}
		
	}
	
	public class Debugging implements StorageDataFileValidator
	{
		private final StorageDataFileValidator delegate;

		Debugging(final StorageDataFileValidator delegate)
		{
			super();
			this.delegate = delegate;
		}

		@Override
		public void validateFile(final ZStorageNumberedFile file, final long fileOffset, final long iterationLength)
		{
			if(!Storage.isDataFile(file))
			{
				return;
			}
			
			XDebug.println("Validating file " + file.identifier() + "[" + fileOffset + ";" + (fileOffset + iterationLength) + "[");
			this.delegate.validateFile(file, fileOffset, iterationLength);
		}
		
	}
	
	
	public static StorageDataFileValidator.Creator Creator(
		final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider,
		final StorageEntityDataValidator.Creator   entityDataValidatorCreator
	)
	{
		return new StorageDataFileValidator.Creator.Default(
			entityDataIteratorProvider,
			entityDataValidatorCreator
		);
	}
	
	@Deprecated
	public static StorageDataFileValidator.Creator CreatorDebugLogging(
		final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider,
		final StorageEntityDataValidator.Creator   entityDataValidatorCreator
	)
	{
		return new StorageDataFileValidator.Creator.DebugLogging(
			entityDataIteratorProvider,
			entityDataValidatorCreator
		);
	}
	
	public interface Creator
	{
		public StorageDataFileValidator createDataFileValidator(
			final StorageTypeDictionary typeDictionary
		);
		
		
		public class Default implements StorageDataFileValidator.Creator
		{
			private final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider;
			private final StorageEntityDataValidator.Creator   entityDataValidatorCreator;
			
			Default(
				final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider,
				final StorageEntityDataValidator.Creator   entityDataValidatorCreator
			)
			{
				super();
				this.entityDataIteratorProvider = entityDataIteratorProvider;
				this.entityDataValidatorCreator = entityDataValidatorCreator;
			}
			
			@Override
			public StorageDataFileValidator createDataFileValidator(
				final StorageTypeDictionary typeDictionary
			)
			{
				return StorageDataFileValidator.New(
					this.entityDataIteratorProvider.provideEntityDataIterator()            ,
					this.entityDataValidatorCreator.createDataFileValidator(typeDictionary),
					StorageFileEntityDataIterator.New()
				);
			}
			
		}
		
		@Deprecated
		public final class DebugLogging extends StorageDataFileValidator.Creator.Default
		{
			DebugLogging(
				final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider,
				final StorageEntityDataValidator.Creator   entityDataValidatorCreator
			)
			{
				super(entityDataIteratorProvider, entityDataValidatorCreator);
			}
			
			@Override
			public StorageDataFileValidator createDataFileValidator(
				final StorageTypeDictionary typeDictionary
			)
			{
				return new StorageDataFileValidator.Debugging(
					super.createDataFileValidator(typeDictionary)
				);
			}
			
		}
		
	}
	
}
