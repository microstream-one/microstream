package one.microstream.hashing;

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

import one.microstream.equality.Equalator;
import one.microstream.equality.IdentityEqualator;
import one.microstream.equality.ValueTypeEqualator;


public interface HashEqualator<T> extends Equalator<T>, Hasher<T>
{
	@Override
	public int hash(T object);

	@Override
	public boolean equal(T object1, T object2);



	public interface Provider<T> extends Equalator.Provider<T>
	{
		@Override
		public HashEqualator<T> provideEqualator();
	}



	public interface ImmutableHashEqualator<E>
	extends HashEqualator<E>, Hasher.ImmutableHashCode<E>
	{
		// type interface only
	}

	public interface IdentityHashEqualator<E>
	extends IdentityEqualator<E>, Hasher.IdentityHashCode<E>, ImmutableHashEqualator<E>
	{
		// type interface only
	}

	public interface ValueTypeHashEqualator<E>
	extends HashEqualator<E>, Hasher.ValueHashCode<E>, ValueTypeEqualator<E>
	{
		// type interface only
	}

	public interface ImmutableValueTypeHashEqualator<E>
	extends ImmutableHashEqualator<E>, ValueTypeHashEqualator<E>
	{
		// type interface only
	}

}
