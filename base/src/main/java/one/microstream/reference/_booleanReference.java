package one.microstream.reference;

/*-
 * #%L
 * microstream-base
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

@FunctionalInterface
public interface _booleanReference
{
	public boolean get();
	
	
	
	public static _booleanReference True()
	{
		// Singleton is an anti-pattern.
		return new True();
	}
	
	public static _booleanReference False()
	{
		// Singleton is an anti-pattern.
		return new False();
	}
	
	public static _booleanReference New(final boolean value)
	{
		return new Default(value);
	}
		
	public final class Default implements _booleanReference
	{
		final boolean value;

		Default(final boolean value)
		{
			super();
			this.value = value;
		}

		@Override
		public final boolean get()
		{
			return this.value;
		}
		
	}
	
	public final class True implements _booleanReference
	{

		@Override
		public final boolean get()
		{
			return true;
		}
		
	}
	
	public final class False implements _booleanReference
	{

		@Override
		public final boolean get()
		{
			return false;
		}
		
	}
	
}
