/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.util.file;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;


@Deprecated // (04.05.2013 TM)NOTE: noob code from years ago
public class FileTraverser
{
	///////////////////////////////////////////////////////////////////////////
	//  static methods   //
	/////////////////////

	// (01.02.2011 TM)NOTE: iteration could be optimized to not always go through this public switch method
	public static void traverseDirectory(
		final File                    directory         ,
		final Predicate<String>       filenameFilter    ,
		final Predicate<? super File> directoryFilter   ,
		final Predicate<? super File> fileFilter        ,
		final Consumer<? super File>  directoryProcessor,
		final Consumer<? super File>  fileProcessor     ,
		final boolean                 deep
	)
	{
		if(!directory.isDirectory())
		{
			// intentionally provoke NPE to also check for non null
			throw new DirectoryException(directory);
		}

		final Consumer<? super File> effDirProcessor;
		if(directoryProcessor == null)
		{
			effDirProcessor = new RecursiveDirectoryProcessor(
				filenameFilter, directoryFilter, fileFilter, fileProcessor, deep
			);
		}
		else
		{
			effDirProcessor = directoryProcessor;
		}

		if(filenameFilter == null)
		{
			if(fileFilter == null)
			{
				internalTraverseDirectory(directory, directoryFilter, effDirProcessor, fileProcessor, deep);
			}
			else
			{
				internalTraverseDirectoryFile(directory, directoryFilter, fileFilter, effDirProcessor, fileProcessor, deep);
			}
		}
		else if(fileFilter == null)
		{
			internalTraverseDirectoryPath(directory, filenameFilter, directoryFilter, effDirProcessor, fileProcessor, deep);
		}
		else
		{
			internalTraverseDirectory(directory, filenameFilter, directoryFilter, fileFilter, effDirProcessor, fileProcessor, deep);
		}
	}


	protected static void internalTraverseDirectory(
		final File directory,
		final Predicate<String> filenameFilter,
		final Predicate<? super File> directoryFilter,
		final Predicate<? super File> fileFilter,
		final Consumer<? super File> directoryProcessor,
		final Consumer<? super File> fileProcessor,
		final boolean deep
	)
	{
		final String[] filenames = directory.list();
		if(filenames == null)
		{
			return; // x_x @ FindBugs
		}

		for(int i = 0, len = filenames.length; i < len; i++)
		{
			final String filename;
			if(!filenameFilter.test(filename = filenames[i]))
			{
				continue;
			}

			final File f;
			if((f = new File(directory, filename)).isDirectory()
			&& (directoryFilter == null || directoryFilter.test(f))
			)
			{
				if(deep)
				{
					directoryProcessor.accept(f);
				}
			}
			else if(fileFilter.test(f))
			{
				fileProcessor.accept(f);
			}
		}
	}

	protected static void internalTraverseDirectoryPath(
		final File directory,
		final Predicate<String> filenameFilter,
		final Predicate<? super File> directoryFilter,
		final Consumer<? super File> directoryProcessor,
		final Consumer<? super File> fileProcessor,
		final boolean deep
	)
	{
		final String[] filenames = directory.list();
		if(filenames == null)
		{
			return; // x_x @ FindBugs
		}

		for(int i = 0, len = filenames.length; i < len; i++)
		{
			final String filename;
			if(!filenameFilter.test(filename = filenames[i]))
			{
				continue;
			}

			final File f;
			if((f = new File(directory, filename)).isDirectory()
			&& (directoryFilter == null || directoryFilter.test(f))
			)
			{
				if(deep)
				{
					directoryProcessor.accept(f);
				}
			}
			else
			{
				fileProcessor.accept(f);
			}
		}
	}

	protected static void internalTraverseDirectoryFile(
		final File directory,
		final Predicate<? super File> directoryFilter,
		final Predicate<? super File> fileFilter,
		final Consumer<? super File> directoryProcessor,
		final Consumer<? super File> fileProcessor,
		final boolean deep
	)
	{
		final String[] filenames = directory.list();
		if(filenames == null)
		{
			return; // x_x @ FindBugs
		}

		for(int i = 0, len = filenames.length; i < len; i++)
		{
			final File f;

			if((f = new File(directory, filenames[i])).isDirectory()
			&& (directoryFilter == null || directoryFilter.test(f))
			)
			{
				if(deep)
				{
					directoryProcessor.accept(f);
				}
			}
			else if(fileFilter.test(f))
			{
				fileProcessor.accept(f);
			}
		}
	}

	protected static void internalTraverseDirectory(
		final File directory,
		final Predicate<? super File> directoryFilter,
		final Consumer<? super File> directoryProcessor,
		final Consumer<? super File> fileProcessor,
		final boolean deep
	)
	{
		final String[] filenames = directory.list();
		if(filenames == null)
		{
			return; // x_x @ FindBugs
		}

		for(int i = 0, len = filenames.length; i < len; i++)
		{
			final File f;
			if((f = new File(directory, filenames[i])).isDirectory()
			&& (directoryFilter == null || directoryFilter.test(f))
			)
			{
				if(deep)
				{
					directoryProcessor.accept(f);
				}
			}
			else
			{
				fileProcessor.accept(f);
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private File                    directory         ;
	private Predicate<String>       filenameFilter    ;
	private Predicate<? super File> directoryFilter   ;
	private Predicate<? super File> fileFilter        ;
	private Consumer<? super File>  directoryProcessor;
	private Consumer<? super File>  fileProcessor     ;
	private boolean                 deep               = true;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public FileTraverser(final Consumer<? super File> fileProcessor)
	{
		this(null, null, null, null, null, fileProcessor, true);
	}

	public FileTraverser(final File directory, final Consumer<? super File> fileProcessor)
	{
		this(directory, null, null, null, null, fileProcessor, true);
	}

	public FileTraverser(
		final File                    directory         ,
		final Predicate<String>       filenameFilter    ,
		final Predicate<? super File> directoryFilter   ,
		final Predicate<? super File> fileFilter        ,
		final Consumer<? super File> directoryProcessor,
		final Consumer<? super File> fileProcessor     ,
		final boolean                 deep
	)
	{
		super();
		this.directory          = directory         ;
		this.filenameFilter     = filenameFilter    ;
		this.directoryFilter    = directoryFilter   ;
		this.fileFilter         = fileFilter        ;
		this.directoryProcessor = directoryProcessor;
		this.fileProcessor      = fileProcessor     ;
		this.deep               = deep              ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public File getDirectory()
	{
		return this.directory;
	}

	public Predicate<String> getFilenameFilter()
	{
		return this.filenameFilter;
	}

	public Predicate<? super File> getDirectoryFilter()
	{
		return this.directoryFilter;
	}

	public Predicate<? super File> getFileFilter()
	{
		return this.fileFilter;
	}

	public boolean isDeep()
	{
		return this.deep;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	public FileTraverser setDirectory(final File directory)
	{
		this.directory = directory;
		return this;
	}

	public FileTraverser setFilenameFilter(final Predicate<String> filenameFilter)
	{
		this.filenameFilter = filenameFilter;
		return this;
	}

	public void setDirectoryFilter(final Predicate<? super File> directoryFilter)
	{
		this.directoryFilter = directoryFilter;
	}

	public FileTraverser setFileFilter(final Predicate<? super File> fileFilter)
	{
		this.fileFilter = fileFilter;
		return this;
	}

	public FileTraverser setDeep(final boolean deep)
	{
		this.deep = deep;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public FileTraverser traverse()
	{
		traverseDirectory(
			this.getDirectory(),
			this.getFilenameFilter(),
			this.getDirectoryFilter(),
			this.getFileFilter(),
			this.getDirectoryProcessor(),
			this.getFileProcessor(),
			this.isDeep()
		);
		return this;
	}

	public FileTraverser traverse(final File directory)
	{
		traverseDirectory(
			directory,
			this.getFilenameFilter(),
			this.getDirectoryFilter(),
			this.getFileFilter(),
			this.getDirectoryProcessor(),
			this.getFileProcessor(),
			this.isDeep()
		);
		return this;
	}

	public FileTraverser traverse(final File directory, final boolean deep)
	{
		traverseDirectory(
			directory,
			this.getFilenameFilter(),
			this.getDirectoryFilter(),
			this.getFileFilter(),
			this.getDirectoryProcessor(),
			this.getFileProcessor(),
			deep
		);
		return this;
	}

	public Consumer<? super File> getDirectoryProcessor()
	{
		return this.directoryProcessor;
	}

	public Consumer<? super File> getFileProcessor()
	{
		return this.fileProcessor;
	}

	public void setDirectoryProcessor(final Consumer<? super File> directoryProcessor)
	{
		this.directoryProcessor = directoryProcessor;
	}

	public void setFileProcessor(final Consumer<? super File> fileProcessor)
	{
		this.fileProcessor = fileProcessor;
	}



	public static class RecursiveDirectoryProcessor implements Consumer<File>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Predicate<String> filenameFilter;
		private final Predicate<? super File> directoryFilter;
		private final Predicate<? super File> fileFilter;
		private final Consumer<? super File> fileProcessor;
		private final boolean deep ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public RecursiveDirectoryProcessor(
			final Predicate<String> filenameFilter,
			final Predicate<? super File> directoryFilter,
			final Predicate<? super File> fileFilter,
			final Consumer<? super File> fileProcessor,
			final boolean deep
		)
		{
			super();
			this.filenameFilter = filenameFilter;
			this.directoryFilter = directoryFilter;
			this.fileFilter = fileFilter;
			this.fileProcessor = fileProcessor;
			this.deep = deep;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final File file)
		{
			traverseDirectory(
				file,
				this.filenameFilter,
				this.directoryFilter,
				this.fileFilter,
				this,
				this.fileProcessor,
				this.deep
			);
		}
	}

}
