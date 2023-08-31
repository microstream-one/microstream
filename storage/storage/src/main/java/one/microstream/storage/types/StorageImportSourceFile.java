package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.afs.types.AFile;


public interface StorageImportSourceFile extends StorageImportSource
{
	public static class Default
	extends    StorageImportSource.Abstract
	implements StorageImportSourceFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ImportSourceFile sourceFile;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final int                               channelIndex,
			final AFile                             file        ,
			final StorageChannelImportBatch.Default headBatch
		)
		{
			super(headBatch);
			this.sourceFile = new ImportSourceFile(file, channelIndex);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public long copyTo(final StorageFile target, final long sourcePosition, final long length)
		{
			return this.sourceFile.copyTo(target, sourcePosition, length);
		}
		
		@Override
		public boolean close()
		{
			return this.sourceFile.close();
		}

		@Override
		public String toString()
		{
			return Integer.toString(this.sourceFile.channelIndex()) + " "
				+ (this.sourceFile.file() == null ? "<Dummy>"  : this.sourceFile.file().toPathString() + " " + this.headBatch)
			;
		}
		
		
		static class ImportSourceFile extends StorageChannelFile.Abstract
		{
			ImportSourceFile(final AFile file, final int channelIndex)
			{
				super(file, channelIndex);
			}
			
		}

	}
	
}
