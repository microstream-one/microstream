package one.microstream.cache.hibernate.types;

/*-
 * #%L
 * microstream-cache-hibernate
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

import one.microstream.chars.XChars;

public enum MissingCacheStrategy
{
	FAIL("fail"),
	CREATE_WARN("create-warn"),
	CREATE("create");

	
	public static MissingCacheStrategy Default()
	{
		return MissingCacheStrategy.CREATE_WARN;
	}
	
	
	private final String externalRepresentation;

	MissingCacheStrategy(
		final String externalRepresentation
	) 
	{
		this.externalRepresentation = externalRepresentation;
	}
	
	public String getExternalRepresentation()
	{
		return this.externalRepresentation;
	}

	public static MissingCacheStrategy ofSetting(Object value) 
	{
		if(value instanceof MissingCacheStrategy) 
		{
			return (MissingCacheStrategy)value;
		}

		final String externalRepresentation = value == null 
			? null 
			: value.toString().trim();

		if(XChars.isEmpty(externalRepresentation)) 
		{
			return MissingCacheStrategy.Default();
		}

		for(MissingCacheStrategy strategy : values()) 
		{
			if(strategy.externalRepresentation.equals(externalRepresentation))
			{
				return strategy;
			}
		}

		throw new IllegalArgumentException("Unrecognized missing cache strategy value : `" + value + '`');
	}
}
