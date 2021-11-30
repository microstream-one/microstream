package one.microstream.afs.types;

/*-
 * #%L
 * microstream-afs
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

import java.util.function.Function;

import one.microstream.X;
import one.microstream.afs.exceptions.AfsExceptionConsistency;
import one.microstream.afs.exceptions.AfsExceptionExclusiveAttemptConflict;
import one.microstream.afs.exceptions.AfsExceptionExclusiveAttemptSharedUserConflict;
import one.microstream.afs.exceptions.AfsExceptionMutation;
import one.microstream.afs.exceptions.AfsExceptionMutationInUse;
import one.microstream.afs.exceptions.AfsExceptionSharedAttemptExclusiveUserConflict;
import one.microstream.chars.XChars;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.interfaces.OptimizableCollection;

public interface AccessManager
{
	public AFileSystem fileSystem();
	
	public boolean isUsed(ADirectory directory);
	
	public boolean isMutating(ADirectory directory);
	
	public boolean isUsed(AFile file);
	
	public boolean isUsedReading(AFile file);
	
	public boolean isUsedWriting(AFile file);
	
	

	public boolean isUsedReading(AFile file, Object user);
	
	public boolean isUsedWriting(AFile file, Object user);
	
	
	public AReadableFile useReading(AFile file, Object user);
	
	public AWritableFile useWriting(AFile file, Object user);
	
	public AReadableFile tryUseReading(AFile file, Object user);
	
	public AWritableFile tryUseWriting(AFile file, Object user);
	
	public AReadableFile downgrade(AWritableFile file);
	
	
	public boolean unregister(AReadableFile file);
	
	public boolean unregister(AWritableFile file);
	
	
	
	
	
	
	public default Object defaultUser()
	{
		return Thread.currentThread();
	}
		
	public default AReadableFile useReading(final AFile file)
	{
		return this.useReading(file, this.defaultUser());
	}
	
	public default AWritableFile useWriting(final AFile file)
	{
		return this.useWriting(file, this.defaultUser());
	}
	
	public default AReadableFile tryUseReading(final AFile file)
	{
		return this.useReading(file, this.defaultUser());
	}
	
	public default AWritableFile tryUseWriting(final AFile file)
	{
		return this.useWriting(file, this.defaultUser());
	}
	
	public <R> R executeMutating(
		ADirectory                      directory,
		Function<? super ADirectory, R> logic
	);
	
	
	
	@FunctionalInterface
	public interface Creator
	{
		public AccessManager createAccessManager(AFileSystem parent);
	}
	
	
	
	public static AccessManager New(final AFileSystem fileSystem)
	{
		return new AccessManager.Default<>(
			notNull(fileSystem)
		);
	}
	
	public class Default<S extends AFileSystem> implements AccessManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final S                               fileSystem         ;
		private final HashTable<ADirectory, DirEntry> usedDirectories    ;
		private final HashTable<ADirectory, Thread>   mutatingDirectories;
		private final HashTable<AFile, FileEntry>     fileUsers          ;
			
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final S fileSystem)
		{
			super();
			this.fileSystem          = fileSystem     ;
			this.usedDirectories     = HashTable.New();
			this.mutatingDirectories = HashTable.New();
			this.fileUsers           = HashTable.New();
		}
		
		
		static final class DirEntry
		{
			final ADirectory directory;
			int usingChildCount;
			
			DirEntry(final ADirectory directory)
			{
				super();
				this.directory = directory;
			}
			
		}
		
		static final class FileEntry
		{
			private static final AReadableFile[] NO_SHARED_USERS = new AReadableFile[0];
		
			private AReadableFile[] sharedUsers = NO_SHARED_USERS;
			
			AWritableFile exclusive;
			
			FileEntry()
			{
				super();
			}
			
			final boolean hasSharedUsers()
			{
				return this.sharedUsers != NO_SHARED_USERS;
			}
			
			final AReadableFile getIfSoleUser(final Object user)
			{
				return this.sharedUsers.length == 1 && this.sharedUsers[0].user() == user
					? this.sharedUsers[0]
					: null
				;
			}
			
			final AReadableFile getForUser(final Object user)
			{
				for(final AReadableFile f : this.sharedUsers)
				{
					if(f.user() == user)
					{
						return f;
					}
				}
				
				return null;
			}
			
			private int indexForUser(final Object user)
			{
				return XArrays.indexOf(user, this.sharedUsers, FileEntry::isForUser);
			}
			
			static final boolean isForUser(final AReadableFile file, final Object user)
			{
				return file.user() == user;
			}
			
			final void add(final AReadableFile file)
			{
				// best performance and common case for first user
				if(this.sharedUsers == NO_SHARED_USERS)
				{
					this.sharedUsers = X.Array(file);
					return;
				}

				// general case: if not yet contained, add.
				if(this.getForUser(file.user()) == null)
				{
					this.sharedUsers = XArrays.add(this.sharedUsers, file);
					return;
				}

				// already contained
			}
			
			final boolean removeShared(final AReadableFile file)
			{
				if(this.sharedUsers.length == 1 && this.sharedUsers[0] == file)
				{
					this.sharedUsers = NO_SHARED_USERS;
					
					return true;
				}
				
				final int index = this.indexForUser(file.user());
				if(index < 0)
				{
					Default.throwUnregisteredException(file, this);
				}
				
				// should never happen since creation/registration checks for that
				if(this.sharedUsers[index] != file)
				{
					throw new AfsExceptionConsistency(
						"Inconsistency detected: to be removed file \""
						+ file
						+ "\" is not the same as the one contained for the same user: \""
						+ this.sharedUsers[index]
						+ "\"."
					);
				}
				
				this.removeIndex(index);
				
				return false;
			}
			
			final void removeForUser(final Object user)
			{
				final int index = this.indexForUser(user);
				if(index < 0)
				{
					return;
				}

				this.removeIndex(index);
			}
			
			private void removeIndex(final int index)
			{
				// must enforce use of empty constant in any case.
				if(index == 0 && this.sharedUsers.length == 1)
				{
					this.sharedUsers = NO_SHARED_USERS;
				}
				else
				{
					this.sharedUsers = XArrays.remove(this.sharedUsers, index);
				}
			}
			
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final Object mutex()
		{
			return this.fileSystem;
		}
		
		@Override
		public final S fileSystem()
		{
			return this.fileSystem;
		}

		@Override
		public boolean isUsed(
			final ADirectory directory
		)
		{
			synchronized(this.mutex())
			{
				return this.usedDirectories.get(ADirectory.actual(directory)) != null;
			}
		}
		
		@Override
		public boolean isMutating(
			final ADirectory directory
		)
		{
			synchronized(this.mutex())
			{
				return this.mutatingDirectories.keys().contains(ADirectory.actual(directory));
			}
		}
		
		@Override
		public boolean isUsed(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null || e.hasSharedUsers();
			}
		}

		@Override
		public boolean isUsedReading(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null || e.hasSharedUsers();
			}
		}
		
		@Override
		public boolean isUsedWriting(final AFile file)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null;
			}
		}
		
		@Override
		public boolean isUsedReading(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive == user || e.getForUser(user) != null;
			}
		}
		
		@Override
		public boolean isUsedWriting(
			final AFile  file,
			final Object user
		)
		{
			synchronized(this.mutex())
			{
				final FileEntry e = this.fileUsers.get(AFile.actual(file));
				if(e == null)
				{
					return false;
				}
				
				return e.exclusive != null && e.exclusive.user() == user;
			}
		}
				
		@Override
		public final <R> R executeMutating(
			final ADirectory                      directory,
			final Function<? super ADirectory, R> logic
		)
		{
			synchronized(this.mutex())
			{
				final ADirectory actual = ADirectory.actual(directory);
				
				// Step 1: check for already existing mutating entry
				final Thread mutatingThread = this.mutatingDirectories.get(actual);
				if(mutatingThread == Thread.currentThread())
				{
					// execute logic WITHOUT removing logic since the call is obviously nested.
					return logic.apply(actual);
				}
				if(mutatingThread != null)
				{
					throw new AfsExceptionMutationInUse(
						"Directory \"" + directory.toPathString() + "\" already used for mutation by \"" + mutatingThread + "\"."
					);
				}
				
				// Step 2: check for already existing using entry
				final Object user = this.usedDirectories.get(actual);
				if(user != null && user != Thread.currentThread())
				{
					throw new AfsExceptionMutationInUse(
						"Directory \"" + directory.toPathString() + "\" already used by \"" + user + "\"."
					);
				}
				
				// Step 3: create mutating entry, execute logic, remove entry in any case.
				this.mutatingDirectories.add(actual, Thread.currentThread());
				try
				{
					return logic.apply(directory);
				}
				finally
				{
					this.mutatingDirectories.removeFor(actual);
				}
			}
		}
				
		@Override
		public AReadableFile useReading(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseReading(file, user, CONFLICT_HANDLER_EXCEPTION);
			}
		}
		
		@Override
		public AReadableFile tryUseReading(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseReading(file, user, CONFLICT_HANDLER_NO_OP);
			}
		}
		
		@Override
		public AWritableFile useWriting(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseWriting(file, user, CONFLICT_HANDLER_EXCEPTION);
			}
		}
		
		@Override
		public AWritableFile tryUseWriting(final AFile file, final Object user)
		{
			synchronized(this.mutex())
			{
				return this.internalUseWriting(file, user, CONFLICT_HANDLER_NO_OP);
			}
		}
		
		private FileEntry createFileEntry(final AFile actual)
		{
			final FileEntry e = new FileEntry();
			this.fileUsers.add(actual, e);
			
			return e;
		}
		
		private AReadableFile registerReading(
			final FileEntry entry ,
			final AFile     actual,
			final Object    user
		)
		{
			final AReadableFile wrapper = this.registerReading(actual, user);
			entry.add(wrapper);
			
			return wrapper;
		}
		
		private AReadableFile registerReading(
			final AFile  actual,
			final Object user
		)
		{
			this.checkForMutatingParents(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());

			return this.fileSystem().wrapForReading(actual, user);
		}
		
		private AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			this.checkForMutatingParents(file.actual(), file.user());
			this.incrementDirectoryUsageCount(file.actual().parent());

			return this.fileSystem().convertToReading(file);
		}
				
		protected final void incrementDirectoryUsageCount(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);
			
			DirEntry entry = this.usedDirectories.get(actual);
			if(entry == null)
			{
				entry = this.addUsedDirectoryEntry(actual);
				
				// new entry means increment usage count for parent incrementally
				if(actual.parent() != null)
				{
					this.incrementDirectoryUsageCount(actual.parent());
				}
			}
			
			entry.usingChildCount++;
			
			// note: child count incrementation on one level does not concern the parent directory count.
		}
		
		private DirEntry addUsedDirectoryEntry(final ADirectory actual)
		{
			final DirEntry entry;
			this.usedDirectories.add(actual, entry = new DirEntry(actual));
			
			return entry;
		}
		
		@Override
		public AReadableFile downgrade(final AWritableFile file)
		{
			synchronized(this.mutex())
			{
				file.validateIsNotRetired();
				
				final AFile actual = AFile.actual(file);
				final FileEntry e = this.fileUsers.get(actual);
				if(e == null)
				{
					throw new IllegalStateException("File not registered as used: " + file.toPathString());
				}
				
				final AReadableFile wrapper = this.convertToReading(file);
				e.add(wrapper);
				
				// may not retire file before conversion since that might need some of file's state.
				this.unregisterExclusive(file, e);
				
				return wrapper;
			}
		}
		
		protected final AReadableFile internalUseReading(
			final AFile           file           ,
			final Object          user           ,
			final ConflictHandler conflictHandler
		)
		{
			final AFile actual = AFile.actual(file);
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return this.registerReading(this.createFileEntry(actual), actual, user);
			}
			
			if(e.exclusive != null)
			{
				if(e.exclusive.user() == user)
				{
					return e.exclusive;
				}
				
				conflictHandler.handleSharedAttemptExclusiveUserConflict(actual, user, e);
			}
			
			AReadableFile wrapper = e.getForUser(user);
			if(wrapper == null)
			{
				wrapper = this.registerReading(e, actual, user);
			}
			
			return wrapper;
		}
		
		protected final AWritableFile internalUseWriting(
			final AFile           file           ,
			final Object          user           ,
			final ConflictHandler conflictHandler
		)
		{
			final AFile actual = AFile.actual(file);
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return this.createFileEntry(actual).exclusive = this.registerWriting(actual, user);
			}
			
			if(e.exclusive != null)
			{
				if(e.exclusive.user() == user)
				{
					return e.exclusive;
				}
				
				conflictHandler.handleExclusiveAttemptConflict(actual, user, e);
				
				return null;
			}
			
			if(e.hasSharedUsers())
			{
				final AReadableFile soleUserFile = e.getIfSoleUser(user);
				if(soleUserFile != null)
				{
					e.exclusive = this.convertToWriting(soleUserFile);
					e.removeForUser(user);
				}
				else
				{
					conflictHandler.handleExclusiveAttemptSharedUsersConflict(actual, user, e);
				}
			}
			else
			{
				e.exclusive = this.registerWriting(actual, user);
			}
			
			return e.exclusive;
		}
		
		private static final ConflictHandler CONFLICT_HANDLER_NO_OP = new ConflictHandler()
		{
			@Override
			public void handleSharedAttemptExclusiveUserConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				// no-op
			}
			
			@Override
			public void handleExclusiveAttemptConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				// no-op
			}
			
			@Override
			public void handleExclusiveAttemptSharedUsersConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				// no-op
			}
		};
		
		static String toStringWithIdentity(final Object o)
		{
			return o == null
				? null
				: "(" + XChars.systemString(o) + ") " + o.toString()
			;
		}
		
		private static final ConflictHandler CONFLICT_HANDLER_EXCEPTION = new ConflictHandler()
		{
			
			@Override
			public void handleSharedAttemptExclusiveUserConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				throw new AfsExceptionSharedAttemptExclusiveUserConflict(
					"File is already exclusively used by a different user: " + actual
					+ ". Exclusive user: " + toStringWithIdentity(entry.exclusive.user())
					+ ". Attempting user: " + toStringWithIdentity(user) + "."
				);
			}
			
			@Override
			public void handleExclusiveAttemptConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				throw new AfsExceptionExclusiveAttemptConflict(
					"File is already used by a different exclusive user: " + actual
					+ ". Exclusive user: " + toStringWithIdentity(entry.exclusive.user())
					+ ". Attempting user: " + toStringWithIdentity(user) + "."
				);
			}
			
			@Override
			public void handleExclusiveAttemptSharedUsersConflict(
				final AFile     actual,
				final Object    user  ,
				final FileEntry entry
			)
			{
				throw new AfsExceptionExclusiveAttemptSharedUserConflict(
					"File \"" + actual.toPathString()
					+ "\" cannot be accessed exclusively since there are shared users present."
				);
			}
		};
		
		
		interface ConflictHandler
		{
			public void handleSharedAttemptExclusiveUserConflict(AFile actual, Object user, FileEntry entry);

			public void handleExclusiveAttemptConflict(AFile actual, Object user, FileEntry entry);

			public void handleExclusiveAttemptSharedUsersConflict(AFile actual, Object user, FileEntry entry);
		}

		
		private AWritableFile registerWriting(
			final AFile  actual,
			final Object user
		)
		{
			this.checkForMutatingParents(actual, user);
			this.incrementDirectoryUsageCount(actual.parent());
						
			return this.fileSystem().wrapForWriting(actual, user);
		}
		
		private AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			this.checkForMutatingParents(file.actual(), file.user());
			this.incrementDirectoryUsageCount(file.actual().parent());

			return this.fileSystem().convertToWriting(file);
		}
		
		private void checkForMutatingParents(final AFile actual, final Object user)
		{
			for(ADirectory p = actual.parent(); p != null; p = p.parent())
			{
				final Thread mutatingThread = this.mutatingDirectories.get(p);
				if(mutatingThread != null && mutatingThread != user)
				{
					throw new AfsExceptionMutation(
						"File \"" + actual.toPathString()
						+ "\" cannot be accessed by user \"" + user + "\" since directory \""
						+ p.toPathString() + "\" is in the process of being changed by user thread \"" + mutatingThread + "\"."
					);
				}
			}
		}
				
		@Override
		public boolean unregister(final AReadableFile file)
		{
			synchronized(this.mutex())
			{
				// logic has to cover writing case, anyway.
				return this.internalUnregister(file);
			}
		}
		
		@Override
		public boolean unregister(final AWritableFile file)
		{
			synchronized(this.mutex())
			{
				return this.internalUnregister(file);
			}
		}
		
		protected boolean internalUnregister(final AReadableFile file)
		{
			final AFile actual = file.actual();
			final FileEntry e = this.fileUsers.get(actual);
			if(e == null)
			{
				return false;
			}
			
			if(!this.internalUnregister(file, e))
			{
				return false;
			}
			
			this.decrementDirectoryUsageCount(actual.parent());
			optimizeMemoryUsage(this.fileUsers);
			
			return true;
		}
		
		private static void optimizeMemoryUsage(final OptimizableCollection collection)
		{
			if((collection.size() & 127) != 0)
			{
				return;
			}

			collection.optimize();
		}
		
		protected void decrementDirectoryUsageCount(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);

			final DirEntry entry = this.getNonNullDirEntry(actual);
			if(--entry.usingChildCount == 0)
			{
				if(actual.parent() != null)
				{
					this.decrementDirectoryUsageCount(actual.parent());
				}
				this.usedDirectories.removeFor(actual);
				optimizeMemoryUsage(this.usedDirectories);
			}
		}
		
		protected final DirEntry getNonNullDirEntry(final ADirectory directory)
		{
			final ADirectory actual = ADirectory.actual(directory);

			final DirEntry entry = this.usedDirectories.get(actual);
			if(entry == null)
			{
				throw new IllegalStateException("Directory not registered as used: " + directory.toPathString());
			}
			
			return entry;
		}
		
		protected boolean internalUnregister(final AReadableFile file, final FileEntry entry)
		{
			// idempotence
			if(file.isRetired())
			{
				return false;
			}
			
			// AWritableFile "is a" AReadableFile, so it could be passed here and must be covered in any case.
			if(file instanceof AWritableFile)
			{
				// exclusive entries never have a shared entry (since they are not shared).
				this.unregisterExclusive((AWritableFile)file, entry);
			}
			else
			{
				this.unregisterShared(file, entry);
			}
			
			return true;
		}
		
		protected void unregisterShared(final AReadableFile file, final FileEntry entry)
		{
			file.retire();
			
			if(entry.removeShared(file) && entry.exclusive == null)
			{
				// if there is no more need for the entry itself, remove it.
				this.fileUsers.removeFor(file.actual());
			}
		}
		
		protected void unregisterExclusive(final AWritableFile file, final FileEntry entry)
		{
			this.validateExclusive(file, entry);
			this.removeExclusive(file, entry);
		}
		
		protected void validateExclusive(final AWritableFile file, final FileEntry entry)
		{
			if(entry.exclusive == file)
			{
				return;
			}
			
			throwUnregisteredException(file, entry);
		}
		
		protected static void throwUnregisteredException(final AReadableFile file, final FileEntry entry)
		{
			throw new AfsExceptionConsistency(
				"Inconsistency detected: attempting to unregister non-retired but not registered file \""
				+ file + "\"."
			);
		}
		
		protected void removeExclusive(final AWritableFile file, final FileEntry entry)
		{
			entry.exclusive = null;
			this.fileUsers.removeFor(file.actual());
			file.retire();
		}
				
	}
	
}
