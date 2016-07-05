package net.jadoth.functional;

import java.util.function.Function;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableMap;
import net.jadoth.collections.types.XImmutableTable;


public final class JadothFunctions
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final transient Function<Object, Object> PASS_THROUGH = new Function<Object, Object>()
	{
		@Override
		public Object apply(final Object input)
		{
			return input;
		}
	};
	
	private static final transient Function<Object, Object> NULL = new Function<Object, Object>()
	{
		@Override
		public Object apply(final Object input)
		{
			return null;
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings("unchecked") // trivial pass through function is always correct for any type
	public static final <T> Function<T, T> passthrough()
	{
		return (Function<T, T>)PASS_THROUGH;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T, R> Function<T, R> toNull()
	{
		return (Function<T, R>)NULL;
	}

	/**
	 * Covariant pass through function that allows an instance of type B extends A to be used as the more
	 * general type A.
	 * When handling direct references, the compiler does this automatically.
	 * However when dealing with explicit type parameters ("Generics"), the compiler is more strict and does not
	 * allow implicit contravariance.
	 * This function reenables this functionality, however by a somewhat clusmy detour.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked") // trivial pass through function is always correct for any type
	public static final <T, S extends T> Function<S, T> covariantPassthrough()
	{
		return (Function<S, T>)PASS_THROUGH;
	}

	/**
	 * Pass-through function with type upcast. Can sometimes be required to correctly handle nested types.
	 * <p>
	 * Consider the following example with V1 extends V:
	 * (e.g. V is an interface and V1 is an implementation of V)
	 * <code><pre>
	 * XMap&lt;K, V1&gt; workingCollection = ... ;
	 * XImmutableMap&lt;K, V&gt; finalCollection = ConstHashTable.NewProjected(input, &lt;K&gt;passthrough(), &lt;V1,V&gt;upcast());
	 * </pre></code>
	 *
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends S, S> Function<T, S> upcast()
	{
		return (Function<T, S>)PASS_THROUGH;
	}

	public static final <K, V> Function<XGettingMap<K, V>, XImmutableMap<K, V>> mapImmurer()
	{
		return new Function<XGettingMap<K, V>, XImmutableMap<K, V>>()
		{
			@Override
			public XImmutableMap<K, V> apply(final XGettingMap<K, V> input)
			{
				return input.immure();
			}
		};
	}

	public static final <K, V> Function<XGettingTable<K, V>, XImmutableTable<K, V>> tableImmurer()
	{
		return new Function<XGettingTable<K, V>, XImmutableTable<K, V>>()
		{
			@Override
			public XImmutableTable<K, V> apply(final XGettingTable<K, V> input)
			{
				return input.immure();
			}
		};
	}



	private JadothFunctions()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
