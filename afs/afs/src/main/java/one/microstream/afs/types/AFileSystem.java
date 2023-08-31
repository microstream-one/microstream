package one.microstream.afs.types;

/*-
 * #%L
 * microstream-afs
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

import java.util.function.Function;

import one.microstream.afs.exceptions.AfsExceptionConsistency;
import one.microstream.afs.exceptions.AfsExceptionMutationInUse;
import one.microstream.afs.exceptions.AfsExceptionUnresolvableRoot;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingTable;

public interface AFileSystem extends AResolving, WriteController
{
	public String defaultProtocol();
	
	public default ADirectory ensureDirectoryPath(final String... pathElements)
	{
		return this.ensureDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory ensureDirectoryPath(String[] pathElements, int offset, int length);
		
	public default AFile ensureFilePath(final String... pathElements)
	{
		return this.ensureFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile ensureFilePath(final String[] directoryPathElements, final String fileIdentifier)
	{
		return this.ensureFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public AFile ensureFilePath(String[] directoryPathElements, int offset, int length, String fileIdentifier);
	
	public AccessManager accessManager();
	
	public ACreator creator();
	
	public AIoHandler ioHandler();
	
	public AReadableFile wrapForReading(AFile file, Object user);

	public AWritableFile wrapForWriting(AFile file, Object user);
	
	public AReadableFile convertToReading(AWritableFile file);
	
	public AWritableFile convertToWriting(AReadableFile file);

	public ADirectory lookupRoot(String identifier);
	
	public ADirectory getRoot(String identifier);

	public ADirectory ensureRoot(String identifier);
	
	public ADirectory ensureRoot(ARoot.Creator rootCreator, String identifier);
	
	/**
	 * Ensures the default root directory. May not be supported by different file system implementations.
	 * @return the root directory
	 * @throws UnsupportedOperationException if the file system doesn't have a default root
	 */
	public ADirectory ensureDefaultRoot();
	
	public ADirectory removeRoot(String identifier);
	
	public boolean addRoot(ADirectory rootDirectory);
	
	public boolean removeRoot(ADirectory rootDirectory);
	
	public <R> R accessRoots(Function<? super XGettingTable<String, ADirectory>, R> logic);
		
	public <I extends AItem> I validateMember(I item);
	
	public default String assemblePath(final AFile file)
	{
		return this.assemblePath(file, VarString.New()).toString();
	}
	
	public default String assemblePath(final ADirectory directory)
	{
		return this.assemblePath(directory, VarString.New()).toString();
	}
	
	public String deriveFileIdentifier(String fileName, String fileType);
	
	public String deriveFileName(String fileIdentifier);
	
	public String deriveFileType(String fileIdentifier);
	

	public VarString assemblePath(AFile file, VarString vs);
	
	public VarString assemblePath(ADirectory directory, VarString vs);
	
	
	public String[] buildPath(AItem item);

	/*
	 * Default implementation assumes items can be handled in a unified way.
	 * If not, the interface allows for switching it around.
	 */
	public default String[] buildPath(final AFile file)
	{
		return this.buildPath((AItem)file);
	}
	
	public default String[] buildPath(final ADirectory directory)
	{
		return this.buildPath((AItem)directory);
	}
	
	
	public String getFileName(AFile file);
	
	public String getFileType(AFile file);

	
	
	public abstract class Abstract<H extends AIoHandler, D, F> implements AFileSystem, AResolver<D, F>, ACreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String                          defaultProtocol;
		private final EqHashTable<String, ADirectory> rootDirectories; // ARoot or relative top-level directory
		private final ACreator                        creator        ;
		private final AccessManager                   accessManager  ;
		private final H                               ioHandler      ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final String defaultProtocol,
			final H      ioHandler
		)
		{
			this(defaultProtocol, null, ioHandler);
		}
		
		protected Abstract(
			final String           defaultProtocol,
			final ACreator.Creator creatorCreator ,
			final H                ioHandler
		)
		{
			this(defaultProtocol, creatorCreator, AccessManager::New, ioHandler);
		}
		
		protected Abstract(
			final String                defaultProtocol     ,
			final ACreator.Creator      creatorCreator      ,
			final AccessManager.Creator accessManagerCreator,
			final H                     ioHandler
		)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.defaultProtocol = defaultProtocol  ;
			this.creator         = this.ensureCreator(creatorCreator);
			this.ioHandler       = ioHandler        ;
			
			// called at the very last just in case the creator needs some of the other state
			this.accessManager = accessManagerCreator.createAccessManager(this);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.ioHandler.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.ioHandler.isWritable();
		}
		
		protected ACreator ensureCreator(final ACreator.Creator creatorCreator)
		{
			return creatorCreator == null
				? this
				: creatorCreator.createCreator(this)
			;
		}
		
		@Override
		public AFileSystem fileSystem()
		{
			return this;
		}
		
		@Override
		public final String defaultProtocol()
		{
			return this.defaultProtocol;
		}
		
		@Override
		public ACreator creator()
		{
			return this.creator;
		}
		
		@Override
		public AccessManager accessManager()
		{
			return this.accessManager;
		}
		
		@Override
		public H ioHandler()
		{
			return this.ioHandler;
		}
		

		@Override
		public final synchronized ADirectory lookupRoot(final String identifier)
		{
			return this.rootDirectories.get(identifier);
		}

		@Override
		public final synchronized ADirectory getRoot(final String identifier)
		{
			final ADirectory existing = this.lookupRoot(identifier);
			if(existing != null)
			{
				return existing;
			}

			throw new AfsExceptionUnresolvableRoot("No root directory found with identifier \"" + identifier + "\".");
		}

		@Override
		public final synchronized ADirectory ensureRoot(final String identifier)
		{
			return this.ensureRoot(this.creator, identifier);
		}
				
		@Override
		public final synchronized <I extends AItem> I validateMember(final I item)
		{
			if(item.fileSystem() == this)
			{
				return item;
			}

			throw new AfsExceptionConsistency(
				"Incompatible parent FileSystem of " + XChars.systemString(item) + ":"
				+ XChars.systemString(item.fileSystem()) + " != this (" + XChars.systemString(this) + ")."
			);
		}
		
		private boolean validateRegisteredRootDirectory(final ADirectory rootDirectory)
		{
			final String rootIdentifier = rootDirectory.identifier();
			final ADirectory registered = this.rootDirectories.get(rootIdentifier);
			if(registered == null)
			{
				return false;
			}
			
			if(registered == rootDirectory)
			{
				return true;
			}
			
			throw new AfsExceptionConsistency(
				"Inconsistent root directories for identifier \"" + rootIdentifier + "\": "
				+ XChars.systemString(registered) + " != " + XChars.systemString(rootDirectory)
			);
		}
		
		private void validateIsUnusedRootDirectory(final ADirectory rootDirectory)
		{
			if(!this.accessManager.isUsed(rootDirectory))
			{
				return;
			}
			
			throw new AfsExceptionMutationInUse(
				"Root directory \"" + rootDirectory.identifier() + " is used an cannot be removed."
			);
		}

		@Override
		public final synchronized ADirectory ensureRoot(
			final ARoot.Creator rootCreator,
			final String        identifier
		)
		{
			ADirectory root = this.rootDirectories.get(identifier);
			if(root != null)
			{
				return root;
			}
			
			root = rootCreator.createRootDirectory(this, identifier);
			this.rootDirectories.add(identifier, root);
			
			return root;
		}
		
		@Override
		public ADirectory ensureDefaultRoot()
		{
			throw new UnsupportedOperationException(
				"This file system implementation (" + this.getClass().getName() +
				") doesn't support a default root. " +
				"Please ensure to create files only in named parent directories."
			);
		}

		@Override
		public final synchronized boolean addRoot(final ADirectory rootDirectory)
		{
			this.validateMember(rootDirectory);
			
			// validate and check for already registered (abort condition)
			if(this.validateRegisteredRootDirectory(rootDirectory))
			{
				return false;
			}
			
			return this.rootDirectories.add(rootDirectory.identifier(), rootDirectory);
		}
		
		@Override
		public final synchronized ADirectory removeRoot(final String name)
		{
			final ADirectory rootDirectory = this.getRoot(name);

			this.removeRoot(rootDirectory);
			
			return rootDirectory;
		}
		
		@Override
		public final synchronized boolean removeRoot(final ADirectory rootDirectory)
		{
			if(!this.validateRegisteredRootDirectory(rootDirectory))
			{
				return false;
			}
			
			this.validateIsUnusedRootDirectory(rootDirectory);
			
			// remove only if no inconsistency was detected.
			this.rootDirectories.removeFor(rootDirectory.identifier());
			
			return true;
		}
		
		@Override
		public final synchronized <R> R accessRoots(
			final Function<? super XGettingTable<String, ADirectory>, R> logic
		)
		{
			return logic.apply(this.rootDirectories);
		}
		
				
		@Override
		public final synchronized ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// getRoot guarantees non-null or exception.
			final ADirectory root = this.getRoot(pathElements[offset]);
			
			return root.resolveDirectoryPath(pathElements, offset + 1, length - 1);
		}
		
		@Override
		public final synchronized ADirectory ensureDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			if(length <= 0)
			{
				return this.ensureDefaultRoot();
			}
			
			XArrays.validateArrayRange(pathElements, offset, length);
						
			final ADirectory root = this.ensureRoot(pathElements[offset]);
			
			ADirectory directory = root;
			for(int o = offset + 1, l = length - 1; l > 0; o++, l--)
			{
				final String pathElement = pathElements[o];
				ADirectory elementDir = directory.getDirectory(pathElement);
				if(elementDir == null)
				{
					elementDir = directory.ensureDirectory(pathElement);
				}
				
				directory = elementDir;
			}
			
			return directory;
		}
		
		@Override
		public final synchronized AFile ensureFilePath(
			final String[] directoryPathElements,
			final int      offset               ,
			final int      length               ,
			final String   fileIdentifier
		)
		{
			final ADirectory directory = this.ensureDirectoryPath(directoryPathElements, offset, length);
			
			AFile file = directory.getFile(fileIdentifier);
			if(file == null)
			{
				file = directory.ensureFile(fileIdentifier);
			}
			
			return file;
		}
		
		protected abstract VarString assembleItemPath(AItem item, VarString vs);
		
		@Override
		public VarString assemblePath(final ADirectory directory, final VarString vs)
		{
			return this.assembleItemPath(directory, vs);
		}
		
		@Override
		public VarString assemblePath(final AFile file, final VarString vs)
		{
			return this.assembleItemPath(file, vs);
		}
		
		@Override
		public String[] buildPath(final AItem item)
		{
			return AItem.buildItemPath(item);
		}

		@Override
		public ARoot createRootDirectory(
			final AFileSystem fileSystem,
			final String      protocol  ,
			final String      identifier
		)
		{
			return ARoot.New(fileSystem, protocol, identifier);
		}
		
	}
	
}
