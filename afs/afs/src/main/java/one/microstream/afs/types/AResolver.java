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

public interface AResolver<D, F>
{
	public AFileSystem fileSystem();
	
	public String[] resolveDirectoryToPath(D directory);
	
	public String[] resolveFileToPath(F file);
	
	public D resolve(ADirectory directory);
	
	public F resolve(AFile file);

	public default ADirectory resolveDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);
		
		return this.fileSystem().resolveDirectoryPath(path);
	}

	public default AFile resolveFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);
		
		return this.fileSystem().resolveFilePath(path);
	}
	
	// (13.05.2020 TM)TODO: priv#49: does ensure~ really belong here?

	public default ADirectory ensureDirectory(final D directory)
	{
		final String[] path = this.resolveDirectoryToPath(directory);
		
		return this.fileSystem().ensureDirectoryPath(path);
	}

	public default AFile ensureFile(final F file)
	{
		final String[] path = this.resolveFileToPath(file);
		
		return this.fileSystem().ensureFilePath(path);
	}
		
}
