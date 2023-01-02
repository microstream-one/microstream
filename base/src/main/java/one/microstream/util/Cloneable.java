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

public interface Cloneable<S>
{
	/**
	 * This method creates a new instance of the same {@link Class}, specified by &lt;S&gt;, of the instance on which
	 * this method is called. Whatever initializations required to create a functional new instance are performed.
	 * <p>
	 * However, this method does NOT create a copy of the current instance. (A common mistake is to confuse cloning
	 * with copying: cloning creates a duplicate with only equal initial state while copying creates a duplicate
	 * with equal full state. Example: a clone of an adult would not be an identical adult, but just an embryo with
	 * equal DNA. A state-wise identical adult would be a copy, not a clone.)
	 * <p>
	 * This method is effectively a constructor called on an existing instance. The use case of such a method is
	 * to eliminate the need to redundantly pass a second instance or constructor if a clone of an instance is needed.
	 * <p>
	 * To indicate the constructor-like character of this method, the pattern of starting the name with a capital letter
	 * is applied to this method
	 * 
	 * @return a clone of this instance.
	 */
	public default S Clone()
	{
		throw new UnsupportedOperationException();
	}
}
