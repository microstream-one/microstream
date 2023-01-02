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

import java.nio.ByteBuffer;

import one.microstream.io.BufferProvider;

public interface AReadableFile extends AFile.Wrapper
{
	/* (31.05.2020 TM)NOTE: shortcut implementations for useReading?
	 * But beware:
	 * - Default user is defined in the accessmanager instance, so it must be used, anyway!
	 * - retired usage/wrapper instances might be used to create new, active ones. May not be suppressed!
	 * - More special cases? Thus: worth it?
	 */
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openReading(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean isOpen()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().isOpen(this);
	}
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean close()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().close(this);
	}

	// implicitly #close PLUS the AFS-management-level aspect
	public default boolean release()
	{
		final boolean wasClosed = this.close();
		
		this.fileSystem().accessManager().unregister(this);
		
		return wasClosed;
	}
	
	@Override
	public default long size()
	{
		// synchronization handled by IoHandler.
		return this.fileSystem().ioHandler().size(this);
	}
	
			

	public default ByteBuffer readBytes()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this);
	}
	
	public default ByteBuffer readBytes(final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position);
	}
	
	public default ByteBuffer readBytes(final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position, length);
	}
	
	
	public default long readBytes(final ByteBuffer targetBuffer)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer);
	}
	
	public default long readBytes(final ByteBuffer targetBuffer, final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position);
	}
	
	public default long readBytes(final ByteBuffer targetBuffer, final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position, length);
	}
	
	
	public default long readBytes(final BufferProvider bufferProvider)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider);
	}
	
	public default long readBytes(final BufferProvider bufferProvider, final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position);
	}
	
	public default long readBytes(final BufferProvider bufferProvider, final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position, length);
	}

	
	
	public default long copyTo(final AWritableFile target)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target);
	}
	
	public default long copyTo(final AWritableFile target, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, target);
	}

	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, length, target);
	}
	
	public boolean retire();
	
	public boolean isRetired();
	
	public void validateIsNotRetired();
		
}
