package one.microstream.afs.sql.types;

/*-
 * #%L
 * microstream-afs-sql
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

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AFileSystem;
import one.microstream.afs.types.AItem;
import one.microstream.afs.types.AReadableFile;
import one.microstream.afs.types.AResolver;
import one.microstream.afs.types.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public interface SqlFileSystem extends AFileSystem, AResolver<SqlPath, SqlPath>
{
	public static SqlPath toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static SqlPath toPath(
		final String... pathElements
	)
	{
		return SqlPath.New(
			notNull(pathElements)
		);
	}


	public static SqlFileSystem New(
		final SqlProvider  provider
	)
	{
		return New(
			SqlConnector.New(provider)
		);
	}

	public static SqlFileSystem New(
		final SqlConnector connector
	)
	{
		return New(
			SqlIoHandler.New(connector)
		);
	}

	public static SqlFileSystem New(
		final SqlIoHandler ioHandler
	)
	{
		return new SqlFileSystem.Default(
			notNull(ioHandler)
		);
	}


	public static class Default extends AFileSystem.Abstract<SqlIoHandler, SqlPath, SqlPath> implements SqlFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final SqlIoHandler ioHandler
		)
		{
			super(
				"jdbc:",
				ioHandler
			);
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String deriveFileIdentifier(
			final String fileName,
			final String fileType
		)
		{
			return XIO.addFileSuffix(fileName, fileType);
		}

		@Override
		public String deriveFileName(
			final String fileIdentifier
		)
		{
			return XIO.getFilePrefix(fileIdentifier);
		}

		@Override
		public String deriveFileType(
			final String fileIdentifier
		)
		{
			return XIO.getFileSuffix(fileIdentifier);
		}

		@Override
		public String getFileName(
			final AFile file
		)
		{
			return XIO.getFilePrefix(file.identifier());
		}

		@Override
		public String getFileType(
			final AFile file
		)
		{
			return XIO.getFileSuffix(file.identifier());
		}

		@Override
		public String[] resolveDirectoryToPath(
			final SqlPath directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final SqlPath file
		)
		{
			return file.pathElements();
		}

		@Override
		public SqlPath resolve(
			final ADirectory directory
		)
		{
			return SqlFileSystem.toPath(directory);
		}

		@Override
		public SqlPath resolve(
			final AFile file
		)
		{
			return SqlFileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return XChars.assembleSeparated(
				vs,
				SqlPath.DIRECTORY_TABLE_NAME_SEPARATOR_CHAR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final SqlPath path = this.resolve(file);
			return SqlReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final SqlPath path = this.resolve(file);
			return SqlWritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return SqlReadableFile.New(
				file                          ,
				file.user()                   ,
				((SqlWritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return SqlWritableFile.New(
				file                          ,
				file.user()                   ,
				((SqlReadableFile)file).path()
			);
		}

	}

}
