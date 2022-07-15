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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import one.microstream.X;
import one.microstream.afs.exceptions.AfsExceptionUnresolvablePathElement;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.functional.XFunc;
import one.microstream.typing.XTypes;

public interface ADirectory extends AItem, AResolving
{
	@Override
	public default String toPathString()
	{
		return this.fileSystem().assemblePath(this);
	}

	@Override
	public default String[] toPath()
	{
		return this.fileSystem().buildPath(this);
	}
	
	public <R> R accessDirectories(Function<? super XGettingTable<String, ? extends ADirectory>, R> logic);
	
	public <R> R accessFiles(Function<? super XGettingTable<String, ? extends AFile>, R> logic);
	
	public <S, R> R accessDirectories(S subject, BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic);
	
	public <S, R> R accessFiles(S subject, BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic);
	
	public boolean registerObserver(ADirectory.Observer observer);
	
	public boolean removeObserver(ADirectory.Observer observer);
	
	public <C extends Consumer<? super ADirectory.Observer>> C iterateObservers(C logic);
	
	public default boolean ensureExists()
	{
		return this.fileSystem().ioHandler().ensureExists(this);
	}
	
	public ADirectory ensureDirectory(String identifier);
	
	public default AFile ensureFile(final String identifier)
	{
		return this.ensureFile(identifier, null, null);
	}
	
	public default AFile ensureFile(final String name, final String type)
	{
		return this.ensureFile(null, name, type);
	}
	
	public AFile ensureFile(String identifier, String name, String type);
		
	public AItem getItem(String identifier);
		
	public ADirectory getDirectory(String identifier);
	
	public AFile getFile(String identifier);
	
	public <C extends Consumer<? super AItem>> C iterateItems(C iterator);
	
	public <C extends Consumer<? super ADirectory>> C iterateDirectories(C iterator);
	
	public <C extends Consumer<? super AFile>> C iterateFiles(C iterator);
		
	public ADirectory inventorize();
	
	public default XGettingEnum<AItem> listItems()
	{
		return AFS.listItems(this, XFunc.all());
	}
	
	public default XGettingEnum<ADirectory> listDirectories()
	{
		return AFS.listDirectories(this, XFunc.all());
	}
	
	public default XGettingEnum<AFile> listFiles()
	{
		return AFS.listFiles(this, XFunc.all());
	}
	
	public boolean contains(AItem item);
	
	public default boolean contains(final ADirectory directory)
	{
		return this.contains((AItem)directory);
	}
	
	public default boolean contains(final AFile file)
	{
		return this.contains((AItem)file);
	}

	public boolean containsDeep(AItem item);
	
	public default boolean containsDeep(final ADirectory directory)
	{
		return this.containsDeep((AItem)directory);
	}
	
	public default boolean containsDeep(final AFile file)
	{
		return this.containsDeep((AItem)file);
	}
	
	
	public boolean containsItem(String itemName);
	
	public boolean containsDirectory(String directoryName);
	
	public boolean containsFile(String fileName);
		
	
	@Override
	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
	
	@Override
	public default boolean exists()
	{
		return this.fileSystem().ioHandler().exists(this);
	}
	
	/**
	 * Removes all child items ({@link ADirectory} or {@link AFile}) that have no physical equivalent.
	 * @return the amount of removed items
	 */
	public int consolidate();
	
	public int consolidateDirectories();

	public int consolidateFiles();
	
	/* (03.06.2020 TM)TODO: priv#49: directory mutation:
	 * - move directory to target directory
	 * - delete directory
	 * - rename directory
	 * 
	 * Each is only allowed if there are no uses for that directory
	 * 
	 * (19.07.2020 TM):
	 * Downgraded to T0D0 since MicroStream does not require directory mutations (for now...).
	 */
	
	/**
	 * Returns true if the directory does not contain any other file or directories
	 * 
	 * @return true if this directory is empty
	 */
	public boolean isEmpty();
	
	public abstract class Abstract
	extends AItem.Abstract
	implements ADirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// static fields //
		//////////////////
		
		private static final ADirectory.Observer[] NO_OBSERVERS = new ADirectory.Observer[0];
		private static final EqHashTable<String, Object> EMPTY = EqHashTable.NewCustom(0);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		@SuppressWarnings("unchecked") // safe due to not containing any elements
		static <T> EqHashTable<String, T> emptyTable()
		{
			return (EqHashTable<String, T>)EMPTY;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private EqHashTable<String, ADirectory> directories = emptyTable();
		private EqHashTable<String, AFile>      files       = emptyTable();

		// memory-optimized array because there should usually be no or very few observers (<= 10).
		private ADirectory.Observer[] observers = NO_OBSERVERS;
		
		// note the 8 bytes cost for this flag due to memory padding. Or there are 7 more bytes "free" for future fields.
		private boolean inventorized;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final String identifier)
		{
			super(identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
			
		@Override
		public final boolean isEmpty() 
		{
			synchronized(this.mutex())
			{
				return this.fileSystem().ioHandler().isEmpty(this);
			}
		}
		
		@Override
		public final AItem getItem(final String identifier)
		{
			synchronized(this.mutex())
			{
				final AFile file = this.getFile(identifier);
				if(file != null)
				{
					return file;
				}
				
				return this.getDirectory(identifier);
			}
		}
		
		private ADirectory internalGetDirectory(final String identifier)
		{
			return this.directories.get(identifier);
		}
		
		@Override
		public final ADirectory getDirectory(final String identifier)
		{
			synchronized(this.mutex())
			{
				return this.internalGetDirectory(identifier);
			}
		}
		
		@Override
		public final AFile getFile(final String identifier)
		{
			synchronized(this.mutex())
			{
				return this.files.get(identifier);
			}
		}
		
		@Override
		public final ADirectory inventorize()
		{
			synchronized(this.mutex())
			{
				this.fileSystem().ioHandler().inventorize(this);
			}
			
			return this;
		}
		
		private void ensureInventorized()
		{
			if(this.inventorized)
			{
				return;
			}
			
			this.inventorize();
			this.inventorized = true;
		}
		
		@Override
		public final <C extends Consumer<? super AItem>> C iterateItems(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.iterateDirectories(iterator);
				this.iterateFiles(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super ADirectory>> C iterateDirectories(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.ensureInventorized();
				this.directories.values().iterate(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super AFile>> C iterateFiles(final C iterator)
		{
			synchronized(this.mutex())
			{
				this.ensureInventorized();
				this.files.values().iterate(iterator);
			}
					
			return iterator;
		}
		
		@Override
		public int consolidate()
		{
			long count = 0;
			synchronized(this.mutex())
			{
				final XGettingEnum<String> physicalItems = this.fileSystem().ioHandler().listItems(this);
				
				count += this.directories.keys().removeBy(dirName ->
					!physicalItems.contains(dirName)
				);
				
				count += this.files.keys().removeBy(fileName ->
					!physicalItems.contains(fileName)
				);
			}
			
			return XTypes.to_int(count);
		}
		
		@Override
		public int consolidateDirectories()
		{
			long count = 0;
			synchronized(this.mutex())
			{
				final XGettingEnum<String> physicalDirectories = this.fileSystem().ioHandler().listDirectories(this);
				
				count += this.directories.keys().removeBy(dirName ->
					!physicalDirectories.contains(dirName)
				);
			}
			
			return XTypes.to_int(count);
		}
		
		@Override
		public int consolidateFiles()
		{
			long count = 0;
			synchronized(this.mutex())
			{
				final XGettingEnum<String> physicalFiles = this.fileSystem().ioHandler().listFiles(this);
				count += this.files.keys().removeBy(fileName ->
					!physicalFiles.contains(fileName)
				);
			}
			
			return XTypes.to_int(count);
		}
		
		@Override
		public final boolean contains(final AItem item)
		{
			// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
			synchronized(this.mutex())
			{
				return item.parent() == this;
			}
		}
		
		@Override
		public boolean containsDeep(final AItem item)
		{
			// cannot lock both since hierarchy order is not clear. But one is sufficient, anyway.
			synchronized(this.mutex())
			{
				for(AItem i = item; (i = i.parent()) != null;)
				{
					if(i == this)
					{
						return true;
					}
				}
				
				return false;
			}
		}
		
		@Override
		public final boolean containsItem(final String itemName)
		{
			synchronized(this.mutex())
			{
				return this.containsFile(itemName)
					|| this.containsDirectory(itemName)
				;
			}
		}
		
		@Override
		public final boolean containsDirectory(final String directoryName)
		{
			synchronized(this.mutex())
			{
				return this.directories.get(directoryName) != null;
			}
		}
		
		@Override
		public final boolean containsFile(final String fileName)
		{
			synchronized(this.mutex())
			{
				return this.files.get(fileName) != null;
			}
		}
		
		private void register(final String identifier, final ADirectory directory)
		{
			if(this.directories == ADirectory.Abstract.<ADirectory>emptyTable())
			{
				this.directories = EqHashTable.New();
			}
			this.directories.add(identifier, directory);
		}
		
		private void register(final String identifier, final AFile file)
		{
			if(this.files == ADirectory.Abstract.<AFile>emptyTable())
			{
				this.files = EqHashTable.New();
			}
			this.files.add(identifier, file);
		}
		
		@Override
		public final ADirectory ensureDirectory(final String identifier)
		{
			synchronized(this.mutex())
			{
				ADirectory directory = this.directories.get(identifier);
				if(directory == null)
				{
					directory = this.fileSystem().creator().createDirectory(this, identifier);
					this.register(identifier, directory);
					// note: inventorize is only called on-demand.
				}
				
				return directory;
			}
		}
		
		@Override
		public final AFile ensureFile(final String identifier, final String name, final String type)
		{
			// either identifier or name must be non-null. Type may be null.
			final String effIdnt, effName, effType;
			
			if(identifier == null)
			{
				effName = notNull(name);
				effType = mayNull(type);
				effIdnt = this.fileSystem().deriveFileIdentifier(name, type);
			}
			else
			{
				effIdnt = identifier;
				effName = name != null
					? name
					: this.fileSystem().deriveFileName(identifier)
				;
				effType = type != null
					? type
					: this.fileSystem().deriveFileType(identifier) // might return null yet again.
				;
			}
			
			synchronized(this.mutex())
			{
				AFile file = this.files.get(effIdnt);
				if(file == null)
				{
					file = this.fileSystem().creator().createFile(this, effIdnt, effName, effType);
					this.register(effIdnt, file);
				}
				
				return file;
			}
		}
		
		@Override
		public final ADirectory resolveDirectoryPath(
			final String[] pathElements,
			final int      offset      ,
			final int      length
		)
		{
			// length means distance in this case. If no more distance remains (length 0), "this" is the result.
			if(length == 0)
			{
				// note: identifier validation makes no sense at this point. Length 0 always means "this".
				return this;
			}
			
			// array bounds validation after trivial / always-correct length 0 case
			XArrays.validateArrayRange(pathElements, offset, length);
			
			// requires the central lock but calls an internal method, so this lock must be acquired here
			synchronized(this.fileSystem())
			{
				ADirectory currentDirectory = this;
				for(int o = offset, l = length; l > 0; o++, l--)
				{
					final ADirectory resolvedChildDirectory = currentDirectory.getDirectory(pathElements[o]);
					if(resolvedChildDirectory == null)
					{
						throw new AfsExceptionUnresolvablePathElement(
							VarString.New()
							.add("Unresolvable path element \"")
							.add(pathElements[offset])
							.add("\" in path \"")
							.addAll(pathElements, VarString::commaSpace)
							.deleteLast(2)
							.add('"', '.')
							.toString()
						);
					}
					
					// recursion implemented as iteration instead of recursive calls (potential stack overflow)
					currentDirectory = resolvedChildDirectory;
				}
				
				return currentDirectory;
			}
		}
		
		@Override
		public final <R> R accessDirectories(
			final Function<? super XGettingTable<String, ? extends ADirectory>, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.directories);
			}
		}
		
		@Override
		public final <R> R accessFiles(
			final Function<? super XGettingTable<String, ? extends AFile>, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.files);
			}
		}

		@Override
		public final<S, R> R accessDirectories(
			final S                                                                     subject,
			final BiFunction<? super XGettingTable<String, ? extends ADirectory>, S, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.directories, subject);
			}
		}
		
		@Override
		public final<S, R> R accessFiles(
			final S                                                                subject,
			final BiFunction<? super XGettingTable<String, ? extends AFile>, S, R> logic
		)
		{
			synchronized(this.mutex())
			{
				return logic.apply(this.files, subject);
			}
		}
		
		@Override
		public final boolean registerObserver(final ADirectory.Observer observer)
		{
			synchronized(this.mutex())
			{
				// best performance and common case for first observer
				if(this.observers == NO_OBSERVERS)
				{
					this.observers = X.Array(observer);
					return true;
				}

				// general case: if not yet contained, add.
				if(!XArrays.contains(this.observers, observer))
				{
					this.observers = XArrays.add(this.observers, observer);
					return true;
				}

				// already contained
				return false;
			}
		}
		
		@Override
		public final boolean removeObserver(final ADirectory.Observer observer)
		{
			synchronized(this.mutex())
			{
				// best performance and special (also weirdly common) case for last/sole observer.
				if(this.observers.length == 1 && this.observers[0] == observer)
				{
					this.observers = NO_OBSERVERS;
					return true;
				}

				// cannot be contained in empty array. Should happen a lot, worth checking.
				if(this.observers == NO_OBSERVERS)
				{
					return false;
				}

				// general case: remove if contained.
				final int index = XArrays.indexOf(observer, this.observers);
				if(index >= 0)
				{
					XArrays.remove(this.observers, index);
					return true;
				}
				
				// not contained.
				return false;
			}
		}
		
		@Override
		public final <C extends Consumer<? super Observer>> C iterateObservers(final C logic)
		{
			synchronized(this.mutex())
			{
				return XArrays.iterate(this.observers, logic);
			}
		}
		
	}
	
	
	public static ADirectory New(final ADirectory parent, final String identifier)
	{
		return new ADirectory.Default(
			notNull(parent),
			notNull(identifier)
		);
	}
	
	public class Default extends Abstract
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ADirectory parent;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final ADirectory parent, final String identifier)
		{
			super(identifier);
			this.parent = parent;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ADirectory parent()
		{
			return this.parent;
		}
		
		@Override
		public final AFileSystem fileSystem()
		{
			return this.parent.fileSystem();
		}
		
	}
		
	// (19.05.2020 TM)TODO: priv#49: call directory Observer directory methods
	public interface Observer
	{
		public void onBeforeFileCreate(AWritableFile fileToCreate);
		
		public void onAfterFileCreate(AWritableFile createdFile);
		
		
		public void onBeforeFileMove(AWritableFile fileToMove, AWritableFile targetFile);
		
		public void onAfterFileMove(AWritableFile movedFile, AWritableFile targetFile);
		
		
		public void onBeforeFileDelete(AWritableFile fileToDelete);
		
		public void onAfterFileDelete(AWritableFile deletedFile, boolean result);


		public void onBeforeDirectoryCreate(ADirectory directoryToCreate);
		
		public void onAfterDirectoryCreate(ADirectory createdDirectory);
		
		
		public void onBeforeDirectoryMove(ADirectory directoryToMove, ADirectory targetDirectory);
		
		public void onAfterDirectoryMove(ADirectory movedDirectory, ADirectory sourceDirectory);
		
		
		public void onBeforeDirectoryDelete(ADirectory directoryToDelete);
		
		public void onAfterDirectoryDelete(ADirectory deletedDirectory, boolean result);
		
	}
	
	
	public static ADirectory actual(final ADirectory directory)
	{
		return directory instanceof ADirectory.Wrapper
			? ((ADirectory.Wrapper)directory).actual()
			: directory
		;
	}
	
	public interface Wrapper extends ADirectory, AItem.Wrapper
	{
		@Override
		public ADirectory actual();
		
	}
	
}
