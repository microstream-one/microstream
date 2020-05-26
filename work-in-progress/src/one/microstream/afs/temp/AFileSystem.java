package one.microstream.afs.temp;

import java.util.function.Function;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingTable;

public interface AFileSystem extends AResolving
{
	public String defaultProtocol();
	
	public default ADirectory ensureDirectoryPath(final String... pathElements)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
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
	
	public IoHandler ioHandler();
	
	public AReadableFile wrapForReading(AFile file, Object user);

	public AWritableFile wrapForWriting(AFile file, Object user);

	// implicitely #close PLUS the AFS-management-level aspect
	public default ActionReport release(final AReadableFile file)
	{
		synchronized(this)
		{
			final boolean wasClosed   = this.ioHandler().close(file);
			final boolean wasReleased = this.accessManager().unregister(file);
			
			return wasClosed
				? wasReleased
					? ActionReport.FULL_ACTION
					: null // impossible / inconsistent
				: wasReleased
					? ActionReport.PART_ACTION
					: ActionReport.NO_ACTION
			;
		}
	}
	

	public ADirectory getRoot(String identifier);

	public ADirectory ensureRoot(String identifier);
	
	public ADirectory ensureRoot(ARoot.Creator rootCreator, String identifier);
	
	public ADirectory removeRoot(String identifier);
	
	public boolean addRoot(ADirectory rootDirectory);
	
	public boolean removeRoot(ADirectory rootDirectory);
	
	public <R> R accessRoots(Function<? super XGettingTable<String, ADirectory>, R> logic);
		
	
	public default String assemblePath(final AFile file)
	{
		return this.assemblePath(file, VarString.New()).toString();
	}
	
	public default String assemblePath(final ADirectory directory)
	{
		return this.assemblePath(directory, VarString.New()).toString();
	}
	

	public VarString assemblePath(AFile file, VarString vs);
	
	public VarString assemblePath(ADirectory directory, VarString vs);
	

	public String[] buildPath(AFile file);
	
	public String[] buildPath(ADirectory directory);
	
	
	public String getFileName(AFile file);
	
	public String getFileType(AFile file);
	
	
	
	public abstract class Abstract<D, F> implements AFileSystem, AResolver<D, F>, ACreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String                          defaultProtocol;
		private final EqHashTable<String, ADirectory> rootDirectories;
		private final ACreator                        creator        ;
		private final AccessManager                   accessManager  ;
		private final IoHandler                       ioHandler      ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final String    defaultProtocol,
			final IoHandler ioHandler
		)
		{
			this(defaultProtocol, null, ioHandler);
		}
		
		protected Abstract(
			final String           defaultProtocol,
			final ACreator.Creator creatorCreator ,
			final IoHandler        ioHandler
		)
		{
			this(defaultProtocol, creatorCreator, AccessManager::New, ioHandler);
		}
		
		protected Abstract(
			final String                     defaultProtocol     ,
			final ACreator.Creator           creatorCreator      ,
			final AccessManager.Creator      accessManagerCreator,
			final IoHandler                  ioHandler
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
		
		protected AResolver<D, F> resolver()
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
		public IoHandler ioHandler()
		{
			return this.ioHandler;
		}
		

		@Override
		public final synchronized ADirectory getRoot(final String identifier)
		{
			final ADirectory existing = this.rootDirectories.get(identifier);
			if(existing != null)
			{
				return existing;
			}

			// (14.05.2020 TM)EXCP: proper exception
			throw new RuntimeException("No root directory found with identifier \"" + identifier + ".");
		}

		@Override
		public final synchronized ADirectory ensureRoot(final String identifier)
		{
			return this.ensureRoot(this.creator, identifier);
		}
		
		private void validateNonExistingRootDirectory(final String identifier)
		{
			final ADirectory existing = this.rootDirectories.get(identifier);
			if(existing == null)
			{
				return;
			}

			// (13.05.2020 TM)EXCP: proper exception
			throw new RuntimeException(
				"Root with identifier \"" + identifier + "\" already exists: " + XChars.systemString(existing)
			);
		}
		
		private void validateParentFileSystem(final AItem item)
		{
			if(item.fileSystem() == this)
			{
				return;
			}

			// (14.05.2020 TM)EXCP: proper exception
			throw new RuntimeException(
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
			
			// (14.05.2020 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent root directories for identifier \"" + rootIdentifier + "\": "
				+ XChars.systemString(registered) + " != " + XChars.systemString(rootDirectory)
			);
		}

		@Override
		public final synchronized ADirectory ensureRoot(
			final ARoot.Creator rootCreator,
			final String        identifier
		)
		{
			this.validateNonExistingRootDirectory(identifier);
			
			final ADirectory created = rootCreator.createRootDirectory(this, identifier);
			this.rootDirectories.add(identifier, created);
			
			return created;
		}

		@Override
		public final synchronized boolean addRoot(final ADirectory rootDirectory)
		{
			this.validateParentFileSystem(rootDirectory);
			
			// validate and check for already registerd (abort condition)
			if(this.validateRegisteredRootDirectory(rootDirectory))
			{
				return false;
			}
			
			return this.rootDirectories.add(rootDirectory.identifier(), rootDirectory);
		}
		
		@Override
		public final synchronized ADirectory removeRoot(final String name)
		{
			return this.rootDirectories.removeFor(name);
		}
		
		@Override
		public final synchronized boolean removeRoot(final ADirectory rootDirectory)
		{
			if(!this.validateRegisteredRootDirectory(rootDirectory))
			{
				return false;
			}
			
			// remove only if no inconcistency was detected.
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
			XArrays.validateArrayRange(pathElements, offset, length);
			
			final ADirectory root = this.ensureRoot(pathElements[offset]);
			
			ADirectory directory = root;
			for(int o = offset + 1, l = length - 1; l > 0; o++, l--)
			{
				final String pathElement = pathElements[o];
				ADirectory elementDir = directory.getDirectory(pathElement);
				if(elementDir == null)
				{
					elementDir = this.creator.createDirectory(directory, pathElement);
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
				file = this.creator.createFile(directory, fileIdentifier);
			}
			
			return file;
		}
		
		@Override
		public synchronized AReadableFile wrapForReading(final AFile file, final Object user)
		{
			final F path = this.resolver().resolve(file);
			
			return AReadableFile.New(file, user, path);
		}

		@Override
		public synchronized AWritableFile wrapForWriting(final AFile file, final Object user)
		{
			final F path = this.resolver().resolve(file);
			
			return AWritableFile.New(file, user, path);
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
		public String[] buildPath(final AFile file)
		{
			return AItem.buildItemPath(file);
		}
		
		@Override
		public String[] buildPath(final ADirectory directory)
		{
			return AItem.buildItemPath(directory);
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
