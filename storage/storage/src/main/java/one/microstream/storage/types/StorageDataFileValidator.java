package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.storage.exceptions.StorageExceptionIncompleteValidation;


public interface StorageDataFileValidator
{
	public void validateFile(
		StorageDataFile file           ,
		long            fileOffset     ,
		long            iterationLength
	);
	
	public default void validateFile(
		final StorageDataFile file
	)
	{
		this.validateFile(file, 0, file.size());
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
		public void validateFile(
			final StorageDataFile file           ,
			final long            fileOffset     ,
			final long            iterationLength
		)
		{				
			final long remainingLength = this.fileIterator.iterateEntityData(
				file,
				fileOffset,
				iterationLength,
				this.entityDataIterator,
				this.entityDataValidator
			);
			if(remainingLength != 0)
			{
				throw new StorageExceptionIncompleteValidation(
					remainingLength + " remaining bytes of " + iterationLength + " total cannot be validated"
					+ " in file " + file.identifier() + " at offset " + fileOffset
				);
			}
		}		
	}
		
	public static StorageDataFileValidator.Creator Creator(
		final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider,
		final StorageEntityDataValidator.Creator   entityDataValidatorCreator,
		final StorageTypeDictionary                storageTypeDictionary
	)
	{
		return new StorageDataFileValidator.Creator.Default(
			entityDataIteratorProvider,
			entityDataValidatorCreator,
			storageTypeDictionary
		);
	}
		
	public interface Creator
	{	
		public StorageDataFileValidator createDataFileValidator();
					
		public class Default implements StorageDataFileValidator.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider;
			private final StorageEntityDataValidator.Creator   entityDataValidatorCreator;
			private final StorageTypeDictionary                storageTypeDictionary;
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(
				final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider,
				final StorageEntityDataValidator.Creator   entityDataValidatorCreator, 
				final StorageTypeDictionary                storageTypeDictionary
			)
			{
				super();
				this.entityDataIteratorProvider = entityDataIteratorProvider;
				this.entityDataValidatorCreator = entityDataValidatorCreator;
				this.storageTypeDictionary      = storageTypeDictionary;
			}
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public StorageDataFileValidator createDataFileValidator() 
			{
				return StorageDataFileValidator.New(
					this.entityDataIteratorProvider.provideEntityDataIterator()            ,
					this.entityDataValidatorCreator.createDataFileValidator(storageTypeDictionary),
					StorageFileEntityDataIterator.New()
				);
			}	
			
		}	
		
	}	
	
}
