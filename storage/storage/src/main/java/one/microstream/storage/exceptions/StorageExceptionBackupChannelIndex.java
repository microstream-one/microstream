package one.microstream.storage.exceptions;

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

public class StorageExceptionBackupChannelIndex extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long channelIndex;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupChannelIndex(final long channelIndex)
	{
		super();
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(final long channelIndex, final String message)
	{
		super(message);
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(final long channelIndex, final Throwable cause)
	{
		super(cause);
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(
		final long      channelIndex,
		final String    message     ,
		final Throwable cause
	)
	{
		super(message, cause);
		this.channelIndex = channelIndex;
	}

	public StorageExceptionBackupChannelIndex(
		final long      channelIndex      ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.channelIndex = channelIndex;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final long channelIndex()
	{
		return this.channelIndex;
	}
	
}
