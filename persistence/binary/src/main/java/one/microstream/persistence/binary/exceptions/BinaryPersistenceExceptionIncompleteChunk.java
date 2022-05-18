package one.microstream.persistence.binary.exceptions;

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

public class BinaryPersistenceExceptionIncompleteChunk extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long currentChunkLength;
	private final long totalChunkLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength)
	{
		this(currentChunkLength, totalChunkLength, null, null);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final String message
	)
	{
		this(currentChunkLength, totalChunkLength, message, null);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final Throwable cause
	)
	{
		this(currentChunkLength, totalChunkLength, null, cause);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final String message, final Throwable cause
	)
	{
		this(currentChunkLength, totalChunkLength, message, cause, true, true);
	}

	public BinaryPersistenceExceptionIncompleteChunk(final long currentChunkLength, final long totalChunkLength,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.currentChunkLength = currentChunkLength;
		this.totalChunkLength   = totalChunkLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getCurrentChunkLength()
	{
		return this.currentChunkLength;
	}

	public long getTotalChunkLength()
	{
		return this.totalChunkLength;
	}



}
