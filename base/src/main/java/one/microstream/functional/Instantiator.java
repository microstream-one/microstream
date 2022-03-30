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

import static one.microstream.X.notNull;

import java.lang.reflect.Constructor;

import one.microstream.exceptions.InstantiationRuntimeException;


public interface Instantiator<T>
{
	public T instantiate() throws InstantiationRuntimeException;
	
	
	
	public static <T> Instantiator<T> WrapDefaultConstructor(final Constructor<T> constructor)
	{
		return new WrappingDefaultConstructor<>(
			notNull(constructor)
		);
	}
	
	public final class WrappingDefaultConstructor<T> implements Instantiator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Constructor<T> constructor;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		WrappingDefaultConstructor(final Constructor<T> constructor)
		{
			super();
			this.constructor = constructor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		@Override
		public T instantiate() throws InstantiationRuntimeException
		{
			try
			{
				return this.constructor.newInstance();
			}
			catch(final InstantiationException e)
			{
				throw new InstantiationRuntimeException(e);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
