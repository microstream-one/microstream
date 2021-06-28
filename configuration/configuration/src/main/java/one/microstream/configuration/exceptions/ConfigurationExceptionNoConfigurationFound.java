package one.microstream.configuration.exceptions;

/*-
 * #%L
 * microstream-configuration
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

public class ConfigurationExceptionNoConfigurationFound extends ConfigurationException
{
	public ConfigurationExceptionNoConfigurationFound(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(null, message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final String message,
		final Throwable cause
	)
	{
		super(null, message, cause);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final String message
	)
	{
		super(null, message);
	}

	public ConfigurationExceptionNoConfigurationFound(
		final Throwable cause
	)
	{
		super(null, cause);
	}

	public ConfigurationExceptionNoConfigurationFound()
	{
		super(null);
	}
	
}
