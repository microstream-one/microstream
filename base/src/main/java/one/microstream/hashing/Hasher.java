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


/**
 * 
 * @param <T> the hashed element's type
 */
public interface Hasher<T>
{
	public int hash(T object);



	/**
	 * Type interface to indicate that the implementation uses {@link Object#hashCode()} (that actually only makes
	 * sense for proper value types, not for entity types).
	 * 
	 * @param <T> the hashed element's type
	 */
	public interface ValueHashCode<T> extends Hasher<T>
	{
		// type interface only
	}

	/**
	 * Type interface to indicate that the implementing {@link Hasher} implementation will always return
	 * the same hash value for the same object.
	 * <br>
	 * This is true for immutable objects (such as instances of {@link String}) or for the identity hash code provided
	 * by {@link System#identityHashCode(Object)}.<br>
	 * The behavior can also be achieved by caching a once created hash code object-externally in the {@link Hasher}
	 * implementation to ensure unchanging hash codes even for objects that are mutable in terms of their
	 * {@link Object#equals(Object)} implemententation.
	 * <p>
	 * The purpose of this marker interface is to indicate that using an implementation of it will not create
	 * hash values that will mess up a hash-based element distribution, which allows certain algorithm optimisations,
	 * for example in hashing collections.
	 * 
	 * @param <T> the hashed element's type
	 * @see IdentityHashCode
	 */
	public interface ImmutableHashCode<T> extends Hasher<T>
	{
		// type interface only
	}

	/**
	 * Type interface to indicate that the implementation uses {@link System#identityHashCode(Object)}.
	 *
	 * @param <T> the hashed element's type
	 */
	public interface IdentityHashCode<T> extends ImmutableHashCode<T>
	{
		// type interface only
	}
	
}
