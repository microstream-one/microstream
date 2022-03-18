package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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

public interface CacheValueValidator
{
	public void validate(
		Object value
	);
	
	
	public static CacheValueValidator New(
		final String slot, 
		final Class<?> expectedType
	)
	{
		return expectedType == null || Object.class.equals(expectedType)
			? new Simple(slot)
			: new Typed(slot, expectedType)
		;
	}
	
	
	public static class Simple implements CacheValueValidator
	{
		final String slot;

		Simple(
			final String slot
		)
		{
			super();
			this.slot = slot;
		}
				
		@Override
		public void validate(
			final Object value
		)
		{
			if(value == null)
			{
				throw new NullPointerException(
					this.slot + " cannot be null"
				);
			}
		}
		
	}
	
	public static class Typed extends Simple
	{
		final Class<?> expectedType;

		Typed(
			final String slot, 
			final Class<?> expectedType
		)
		{
			super(slot);
			this.expectedType = expectedType;
		}
		
		@Override
		public void validate(
			final Object value
		)
		{
			super.validate(value); // null check
			
			if(!this.expectedType.isInstance(value))
			{
				throw new ClassCastException(
					"Type mismatch for " + this.slot + ": " + 
					value + " <> " + this.expectedType.getName()
				);
			}
		}
		
	}
	
}
