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

import one.microstream.math.XMath;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


/**
 * Utility methods related to hashing.
 *
 */
public final class XHashing
{
	/* it is important for the singleton to have its own proper named type
	 * to make the type indifferent to declaration order, e.g. for purposes of persisting metadata.
	 */
	static final class SingletonIdentityHashEqualator implements HashEqualator.IdentityHashEqualator<Object>
	{
		SingletonIdentityHashEqualator()
		{
			super();
		}

		@Override
		public final int hash(final Object object)
		{
			return System.identityHashCode(object);
		}

		@Override
		public final boolean equal(final Object object1, final Object object2)
		{
			return object1 == object2;
		}
	}

	static final class SingletonValueHashEqualator implements HashEqualator.ValueTypeHashEqualator<Object>
	{
		SingletonValueHashEqualator()
		{
			super();
		}

		@Override
		public final int hash(final Object object)
		{
			return object == null ? 0 : object.hashCode();
		}

		@Override
		public final boolean equal(final Object object1, final Object object2)
		{
			return object1 == object2 || object1 != null && object1.equals(object2);
		}
	}

	static final class NonNullSingletonValueHashEqualator implements HashEqualator.ValueTypeHashEqualator<Object>
	{
		NonNullSingletonValueHashEqualator()
		{
			super();
		}

		@Override
		public final int hash(final Object object)
		{
			return object.hashCode();
		}

		@Override
		public final boolean equal(final Object object1, final Object object2)
		{
			return object1.equals(object2);
		}
	}

	static final class SingletonKeyValueIdentityHashEqualator
	implements HashEqualator.IdentityHashEqualator<KeyValue<Object, Object>>
	{
		SingletonKeyValueIdentityHashEqualator()
		{
			super();
		}

		@Override
		public final int hash(final KeyValue<Object, Object> kv)
		{
			return System.identityHashCode(kv.key());
		}

		@Override
		public final boolean equal(final KeyValue<Object, Object> kv1, final KeyValue<Object, Object> kv2)
		{
			return kv1.key() == kv2.key();
		}
	}


	/**
	 * Central stateless {@link HashEqualator} function instance with identity hash equality.
	 */
	static final SingletonIdentityHashEqualator HASH_EQUALITY_IDENTITY = new SingletonIdentityHashEqualator();

	/**
	 * Central stateless {@link HashEqualator} function instance with
	 * {@link Object#hashCode()} / {@link Object#equals(Object)} equlity (value type equality).
	 */
	static final SingletonValueHashEqualator HASH_EQUALITY_VALUE = new SingletonValueHashEqualator();

	static final SingletonKeyValueIdentityHashEqualator HASH_EQUALITY_IDENTITY_KV =
		new SingletonKeyValueIdentityHashEqualator()
	;

	@SuppressWarnings("unchecked")
	public static final <E> HashEqualator<E> hashEqualityIdentity()
	{
		return (HashEqualator<E>)HASH_EQUALITY_IDENTITY;
	}

	@SuppressWarnings("unchecked")
	public static final <E> HashEqualator<E> hashEqualityValue()
	{
		return (HashEqualator<E>)HASH_EQUALITY_VALUE;
	}

	@SuppressWarnings("unchecked")
	public static final <KV extends KeyValue<?, ?>> HashEqualator<KV> keyValueHashEqualityKeyIdentity()
	{
		return (HashEqualator<KV>)HASH_EQUALITY_IDENTITY_KV;
	}
	
	public static final int calculateHashLength(final long desiredCapacity, final float hashDensity)
	{
		return padHashLength((long)Math.ceil(desiredCapacity / hashDensity));
	}

	public static final int padHashLength(final long desiredHashLength)
	{
		if(XMath.isGreaterThanOrEqualHighestPowerOf2(desiredHashLength))
		{
			// (technical) magic value. Cannot be higher due to hashing bit arithmetic.
			return XMath.highestPowerOf2_int();
		}
		
		int capacity = 1;
		while(capacity < desiredHashLength)
		{
			capacity <<= 1;
		}
		
		return capacity;
	}
	
	public static final boolean isValidHashDensity(final float hashDensity)
	{
		return hashDensity > 0 && !Float.isNaN(hashDensity);
	}

	public static final float validateHashDensity(final float hashDensity)
	{
		if(!isValidHashDensity(hashDensity))
		{
			throw new IllegalArgumentException("Illegal hash density: " + hashDensity);
		}
		
		return hashDensity;
	}

	/**
	 * Wrappers the passed {@link HashEqualator} instance as a {@link KeyValue} {@link HashEqualator} instance, using
	 * the {@link KeyValue} instance's key as the hash element.
	 * 
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param hashEqualator single element {@link HashEqualator} instance to be wrapped.
	 * @return the passed instance as a {@link KeyValue} {@link HashEqualator} instance.
	 */
	public static final <K, V> HashEqualator<KeyValue<K, V>> wrapAsKeyValue(
		final HashEqualator<? super K> hashEqualator
	)
	{
		return new HashEqualator<KeyValue<K, V>>()
		{
			@Override
			public int hash(final KeyValue<K, V> kv)
			{
				return kv == null ? 0 : hashEqualator.hash(kv.key());
			}

			@Override
			public boolean equal(final KeyValue<K, V> kv1, final KeyValue<K, V> kv2)
			{
				return kv1 != null && kv2 != null && hashEqualator.equal(kv1.key(), kv2.key());
			}
		};
	}

	public static final <E> HashEqualator<E> deriveHashEquality(final Class<E> type)
	{
		return XTypes.isValueType(type)
			? XHashing.<E>hashEqualityValue()
			: XHashing.<E>hashEqualityIdentity()
		;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XHashing()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
