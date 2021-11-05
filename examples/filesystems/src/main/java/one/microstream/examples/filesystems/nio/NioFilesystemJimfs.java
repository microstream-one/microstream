
package one.microstream.examples.filesystems.nio;

/*-
 * #%L
 * microstream-examples-filesystems
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

import java.nio.file.FileSystem;

import com.google.common.jimfs.Jimfs;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageLiveFileProvider;


public class NioFilesystemJimfs
{
	@SuppressWarnings("unused")
	public static void main(
		final String[] args
	)
	{
		// create jimfs filesystem
		final FileSystem             jimfs   = Jimfs.newFileSystem();
		
		// start storage with jimfs path
		final EmbeddedStorageManager storage = EmbeddedStorage.start(jimfs.getPath("storage"));
		storage.shutdown();
		
		// or create file provider with jimsfs filesytem for further configuration
		final NioFileSystem           fileSystem   = NioFileSystem.New(jimfs);
		final StorageLiveFileProvider fileProvider = StorageLiveFileProvider.Builder(fileSystem)
			.setDirectory(fileSystem.ensureDirectoryPath("storage"))
			.createFileProvider()
		;
	}
}
