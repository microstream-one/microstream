package one.microstream.reference;

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

import one.microstream.collections.Singleton;


/**
 * Simple Reference class to handle mutable references. Handle with care!
 * <p>
 * Note: In most cases, a mutable reference object like this should not be necessary if the program is well
 * structured (that's why no such class exists in the Java API).
 * Extensive use of this class where it would be better to restructure the program may end in even more structural
 * problems.<br>
 * Yet in some cases, a mutable reference really is needed or at least helps in creating cleaner structures.<br>
 * So again, use wisely.
 */
public interface Reference<T> extends Referencing<T>
{
	public void set(T object);



	public static <T> Reference<T> New(final T object)
	{
		return Singleton.New(object);
	}

}
