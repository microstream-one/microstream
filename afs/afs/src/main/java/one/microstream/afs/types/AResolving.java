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

public interface AResolving
{
	// note: no single string parameter resolving here, since this type is separator-agnostic.
	
	public default AFile resolveFilePath(
		final String... pathElements
	)
	{
		return this.resolveFilePath(pathElements, 0, pathElements.length - 1, pathElements[pathElements.length - 1]);
	}
	
	public default AFile resolveFilePath(
		final String[] directoryPathElements,
		final String   fileIdentifier
	)
	{
		return this.resolveFilePath(directoryPathElements, 0, directoryPathElements.length, fileIdentifier);
	}
	
	public default AFile resolveFilePath(
		final String[] directoryPathElements,
		final int      offset               ,
		final int      length               ,
		final String   fileIdentifier
	)
	{
		final ADirectory directory = this.resolveDirectoryPath(directoryPathElements, offset, length);
		
		// if the implementation of #resolveDirectoryPath returns null, then conform to this strategy.
		return directory == null
			? null
			: directory.getFile(fileIdentifier)
		;
	}
	
	
	public default ADirectory resolveDirectoryPath(
		final String... pathElements
	)
	{
		return this.resolveDirectoryPath(pathElements, 0, pathElements.length);
	}

	public ADirectory resolveDirectoryPath(String[] pathElements, int offset, int length);
			
}
