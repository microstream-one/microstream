package one.microstream.storage.exceptions;

/*-
 * #%L
 * microstream-storage
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

public final class StorageExceptionInvalidFile extends StorageException
{
	private final long fileLength;

	public StorageExceptionInvalidFile(final long fileLength)
	{
		super();
		this.fileLength = fileLength;
	}

	public StorageExceptionInvalidFile(final long fileLength, final Throwable cause)
	{
		super(cause);
		this.fileLength = fileLength;
	}

	public final long fileLength()
	{
		return this.fileLength;
	}

}
