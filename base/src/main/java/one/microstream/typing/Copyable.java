package one.microstream.typing;

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

/**
 * Copyable objects can create copies of themselves that will have the same type and behave exactly as they do.
 * <p>
 * This does not necessarily mean that all data is copied. E.g. caching fields or ones that are set lazy on
 * demand could be left out in the copy process.
 *
 */
public interface Copyable
{
	public Copyable copy();

	public final class Static
	{
		/**
		 * Returns either {@code null} if the passed instance is {@code null}, otherwise returns the instance created by
		 * the call to {@link Copyable#copy()}.
		 *
		 * @param <T> The type of the {@link Copyable} instance.
		 * @param copyable the instance whose {@link Copyable#copy()} method shall be called to create the copy.
		 * @return the copy created by the call to the {@link Copyable#copy()} method from the passed instance.
		 */
		@SuppressWarnings("unchecked")
		public static <T extends Copyable> T copy(final T copyable)
		{
			return copyable == null ? null : (T)copyable.copy(); // cast must be valid as defined by contract.
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException when called
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
	
}
