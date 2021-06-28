
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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

public interface Unwrappable
{
	public <T> T unwrap(final Class<T> clazz);
	
	final class Static
	{
		public static <T> T unwrap(final Object subject, final Class<T> clazz)
		{
			if(clazz.isAssignableFrom(subject.getClass()))
			{
				return clazz.cast(subject);
			}
			throw new IllegalArgumentException("Unwrapping to " + clazz + " is not supported by this implementation");
		}
		
		private Static()
		{
			throw new Error();
		}
		
	}
	
}
