package one.microstream.functional;

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

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.reflect.XReflect;

public interface DefaultInstantiator
{
	public <T> T instantiate(Class<T> type) throws InstantiationRuntimeException;
	
		
	
	public static DefaultInstantiator.Default Default()
	{
		return new Default();
	}
	
	public final class Default implements DefaultInstantiator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
		{
			return XReflect.defaultInstantiate(type);
		}
		
	}
	
}
