package one.microstream.storage.configuration;

/*-
 * #%L
 * microstream-storage-embedded-configuration
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

/**
 * 
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
public class InvalidStorageConfigurationException extends StorageConfigurationException
{
	public InvalidStorageConfigurationException()
	{
		super();
	}

	public InvalidStorageConfigurationException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(
			message           ,
			cause             ,
			enableSuppression ,
			writableStackTrace
		);
	}

	public InvalidStorageConfigurationException(
		final String    message,
		final Throwable cause
	)
	{
		super(
			message,
			cause
		);
	}

	public InvalidStorageConfigurationException(
		final String message
	)
	{
		super(message);
	}

	public InvalidStorageConfigurationException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
