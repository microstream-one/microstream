package one.microstream.util;

/*-
 * #%L
 * microstream-base
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

import java.lang.management.ManagementFactory;

/**
 * Provides an arbitrary identity string that is unique for an individual process across any number of systems.
 *
 */
public interface ProcessIdentityProvider
{
	public String provideProcessIdentity();
	
	
	
	public static String queryProcessIdentity()
	{
		// quickly googled solution that is assumed to be "good enough" until proven otherwise.
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	
	
	public static ProcessIdentityProvider New()
	{
		return new ProcessIdentityProvider.Default();
	}
	
	public final class Default implements ProcessIdentityProvider
	{
		Default()
		{
			super();
		}

		@Override
		public String provideProcessIdentity()
		{
			return ProcessIdentityProvider.queryProcessIdentity();
		}
		
	}
}
