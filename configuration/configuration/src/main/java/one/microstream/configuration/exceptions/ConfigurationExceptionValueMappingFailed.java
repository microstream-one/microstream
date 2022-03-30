package one.microstream.configuration.exceptions;

/*-
 * #%L
 * microstream-configuration
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

import one.microstream.configuration.types.Configuration;

public class ConfigurationExceptionValueMappingFailed extends ConfigurationException
{
	private final String key  ;
	private final String value;
	
	public ConfigurationExceptionValueMappingFailed(
		final Configuration configuration,
		final String key,
		final String value
	)
	{
		super(configuration);
		this.key   = key;
		this.value = value;
	}

	public ConfigurationExceptionValueMappingFailed(
		final Configuration configuration,
		final Throwable cause,
		final String key,
		final String value
	)
	{
		super(configuration, cause);
		this.key   = key;
		this.value = value;
	}
	
	
	public String key()
	{
		return this.key;
	}
	
	public String value()
	{
		return this.value;
	}
	
}
