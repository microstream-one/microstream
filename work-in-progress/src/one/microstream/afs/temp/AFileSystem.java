package one.microstream.afs.temp;

import java.util.function.Function;

import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingTable;

public interface AFileSystem extends AResolving
{
	/* (30.04.2020 TM)FIXME: priv#49: "protocol" here or in AccessManager
	 * Or is "protocol" a trait of a root directory?
	 * With a FileSystem instance being able to contain roots with different protocols?
	 * Example:
	 * "file://C:/"
	 * "file://C:/"
	 * "https://some.cloudstorage.com/storage12343534/"
	 * 
	 * HMMM....
	 */
	
	// (04.05.2020 TM)TODO: priv#49: #resolve methods with root String?
	
	/* (04.05.2020 TM)TODO: priv#49: #resolve methods with single String that gets parsed?
	 * Or is that the job of an APathResolver?
	 */
	

	
	
	
	public default ADirectory ensureDirectoryPath(final String... pathElements)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory ensureDirectoryPath(String[] pathElements, int offset, int length);
		
	public default AFile ensureFilePath(final String... pathElements)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile ensureFilePath(final String[] directoryPathElements, final String fileIdentifier)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public AFile ensureFilePath(String[] directoryPathElements, int offset, int length, String fileIdentifier);
	
	public AccessManager accessManager();
	
	public IoHandler ioHandler();

	// implicitely #close PLUS the AFS-management-level aspect
	public default ActionReport release(final AReadableFile file)
	{
		synchronized(file)
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
		
	
	
	
	public class Abstract<D, F> implements AFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<String, ADirectory> rootDirectories;
		private final AResolver<D, F>                 resolver       ;
		private final ACreator                        creator        ;
		private final AccessManager                   accessManager  ;
		private final IoHandler                       ioHandler      ;
		
		// (09.05.2020 TM)FIXME: priv#49: Lock FileSystem for creating new Items or just their parent directory?
		// (13.05.2020 TM)FIXME: priv#49: include resolver here via Generics typing?
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Abstract(
			final AResolver<D, F>       resolver            ,
			final ACreator              creator             ,
			final AccessManager.Creator accessManagerCreator,
			final IoHandler             ioHandler
		)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.resolver        = resolver         ;
			this.creator         = creator          ;
			this.ioHandler       = ioHandler        ;
			
			// called at the very last just in case the creator needs some of the other state
			this.accessManager = accessManagerCreator.createAccessManager(this);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
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
		public final synchronized ADirectory ensureRoot(final ARoot.Creator rootCreator, final String identifier)
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
		public final synchronized <R> R accessRoots(final Function<? super XGettingTable<String, ADirectory>, R> logic)
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
			// getRoot guarantees non-null
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
			
			
			
			// FIXME AFileSystem.Abstract#ensureDirectoryPath()
			throw new one.microstream.meta.NotImplementedYetError();
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
			
			synchronized(directory)
			{
				AFile file = directory.getFile(fileIdentifier);
				if(file == null)
				{
					file = this.accessManager.executeMutating(directory, d ->
						this.creator.createFile(d, fileIdentifier)
					);
				}
				
				return file;
			}
		}
		
	}
	
}
