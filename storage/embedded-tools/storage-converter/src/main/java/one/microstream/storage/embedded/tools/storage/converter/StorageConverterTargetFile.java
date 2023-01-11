package one.microstream.storage.embedded.tools.storage.converter;

/*-
 * #%L
 * MicroStream Embedded Storage Tools Converter
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

import java.nio.ByteBuffer;

import one.microstream.afs.types.AWritableFile;

/**
 * Helper to hold all information of a single target "File"
 * of a {@link StorageConverterTarget} a on place.
 */
public class StorageConverterTargetFile
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final AWritableFile file;
	private final long          fileNumber;
	private       long          size;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Create a {@link StorageConverterTargetFile} instance.
	 * 
	 * @param file the target storage file
	 * @param fileNumber the file number
	 */
	public StorageConverterTargetFile(final AWritableFile file, final long fileNumber)
	{
		super();
		this.file = file;
		this.fileNumber = fileNumber;
	}

	public void writeBytes(final ByteBuffer buffer)
	{
		this.size += this.file.writeBytes(buffer);
	}

	public long fileNumber()
	{
		return this.fileNumber;
	}

	public long size()
	{
		return this.size;
	}

	public void release()
	{
		this.file.release();
	}

}
