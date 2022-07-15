package one.microstream.persistence.internal;

/*-
 * #%L
 * microstream-persistence
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

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AReadableFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionSource;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;

public class PersistenceTypeDictionaryFileHandler implements PersistenceTypeDictionaryIoHandler
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final String readTypeDictionary(final AFile file)
	{
		return readTypeDictionary(file, null);
	}

	public static final String readTypeDictionary(final AFile file, final String defaultString)
	{
		try
		{
			if(!file.exists())
			{
				return defaultString;
			}
			
			final AReadableFile rFile = file.useReading();
			
			try
			{
				final ByteBuffer bb = rFile.readBytes();
				
				return XChars.String(bb, Persistence.standardCharset());
			}
			finally
			{
				rFile.release();
			}
		}
		catch(final Exception e)
		{
			throw new PersistenceExceptionSource(e);
		}
	}

	public static final void writeTypeDictionary(final AFile file, final String typeDictionaryString)
	{
		try
		{
			final AWritableFile wFile = file.useWriting();
			if(wFile.exists())
			{
				wFile.truncate(0);
			}
			else
			{
				wFile.create();
			}
			
			try
			{
				final byte[] bytes = typeDictionaryString.getBytes(Persistence.standardCharset());
				final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
				wFile.writeBytes(X.List(dbb));
			}
			finally
			{
				wFile.release();
			}
		}
		catch(final Exception t)
		{
			throw new PersistenceException(t);
		}
	}
	
	@Deprecated
	public static PersistenceTypeDictionaryFileHandler NewInDirectory(final ADirectory directory)
	{
		return New(directory);
	}
	
	public static PersistenceTypeDictionaryFileHandler New(final ADirectory directory)
	{
		return New(directory, null);
	}
	
	public static PersistenceTypeDictionaryFileHandler New(final AFile file)
	{
		return New(file, null);
	}
	
	@Deprecated
	public static PersistenceTypeDictionaryFileHandler NewInDirectory(
		final ADirectory                      directory    ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return New(directory, writeListener);
	}
	
		
	public static PersistenceTypeDictionaryFileHandler New(
		final ADirectory                      directory    ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return new PersistenceTypeDictionaryFileHandler(
			directory.ensureFile(Persistence.defaultFilenameTypeDictionary()),
			mayNull(writeListener)
		);
	}
	
	public static PersistenceTypeDictionaryFileHandler New(
		final AFile                           file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return new PersistenceTypeDictionaryFileHandler(
			notNull(file)         ,
			mayNull(writeListener)
		);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile                           file         ;
	private final PersistenceTypeDictionaryStorer writeListener;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceTypeDictionaryFileHandler(
		final AFile                           file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		super();
		this.file          = file         ;
		this.writeListener = writeListener;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	protected AFile file()
	{
		return this.file;
	}

	@Override
	public final synchronized String loadTypeDictionary()
	{
		return readTypeDictionary(this.file);
	}
	
	protected synchronized void writeTypeDictionary(final String typeDictionaryString)
	{
		writeTypeDictionary(this.file, typeDictionaryString);
	}

	@Override
	public final synchronized void storeTypeDictionary(final String typeDictionaryString)
	{
		this.writeTypeDictionary(typeDictionaryString);
		if(this.writeListener != null)
		{
			this.writeListener.storeTypeDictionary(typeDictionaryString);
		}
	}
	
	
	@FunctionalInterface
	public interface Creator
	{
		public PersistenceTypeDictionaryIoHandler createTypeDictionaryIoHandler(
			AFile                           file         ,
			PersistenceTypeDictionaryStorer writeListener
		);
		
	}
	
	
	public static PersistenceTypeDictionaryFileHandler.Provider ProviderInDirectory(final ADirectory directory)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			directory.ensureFile(Persistence.defaultFilenameTypeDictionary())
		);
	}
	
	public static PersistenceTypeDictionaryFileHandler.Provider Provider(final AFile file)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			notNull(file)
		);
	}
	
	public static final class Provider implements PersistenceTypeDictionaryIoHandler.Provider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFile file;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Provider(final AFile file)
		{
			super();
			this.file = file;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionaryFileHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			return PersistenceTypeDictionaryFileHandler.New(this.file);
		}
		
	}

}
