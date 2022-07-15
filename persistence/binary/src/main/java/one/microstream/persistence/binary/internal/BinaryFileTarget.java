package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
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

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AFileSystem;
import one.microstream.collections.ArrayView;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

public class BinaryFileTarget implements PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile       file;
	private final AFileSystem fs  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileTarget(final AFile file)
	{
		super();
		this.file = notNull(file);
		this.fs   = file.fileSystem();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void write(final Binary chunk) throws PersistenceExceptionTransfer
	{
		try
		{
			this.validateIsWritable();
			
			final ArrayView<ByteBuffer> buffers = X.ArrayView(chunk.buffers());
			AFS.executeWriting(this.file, wf ->
				wf.writeBytes(buffers)
			);
		}
		catch(final Exception e)
		{
			throw new PersistenceException(e);
		}
	}
	
	@Override
	public final void validateIsWritable()
	{
		this.fs.validateIsWritable();
	}
	
	@Override
	public final boolean isWritable()
	{
		return this.fs.isWritable();
	}
	
}

