package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceTypeHandler;

public interface BinaryTypeHandler<T> extends PersistenceTypeHandler<Binary, T>
{
	// (18.03.2013)NOTE: reasonable information, but currently not used. Maybe it's superfluous in proper structure?
//	public long getFixedBinaryContentLength();

	/**
	 * Provides information if two instances of the handled type can have different binary length.<p>
	 * Examples for variable length types:
	 * <ul>
	 * <li> arrays</li>
	 * <li>{@code java.lang.String}</li>
	 * <li>{@code java.util.ArrayList}</li>
	 * <li>{@code java.Math.BigDecimal}</li>
	 * </ul><p>
	 * Examples for fixed length types:
	 * <ul>
	 * <li>primitive value wrapper types</li>
	 * <li>{@code java.lang.Object}</li>
	 * <li>{@code java.util.Date}</li>
	 * <li>typical entity types (without unshared inlined variable length component instances)</li>
	 * </ul>
	 *
	 * @return
	 */
	public boolean isVariableBinaryLengthType();

	/**
	 * Provides information if one particular instance can have variing binary length from one store to another.<p>
	 * Examples for variable length instances:
	 * <ul>
	 * <li> variable size collection instances</li>
	 * <li> variable size pesudo collection instances like {@code java.util.StringBuilder}</li>
	 * <li> instances of custom defined types similar to collections</li>
	 * </ul><p>
	 * Examples for fixed length instances:
	 * <ul>
	 * <li>arrays</li>
	 * <li>all immutable type instances (like {@code java.lang.String} )</li>
	 * <li>all fixed length types (see {@link #isVariableBinaryLengthType()}</li>
	 * </ul>
	 *
	 * @return
	 */
	public boolean hasVariableBinaryLengthInstances();



	public abstract class AbstractImplementation<T>
	extends PersistenceTypeHandler.AbstractImplementation<Binary, T>
	implements BinaryTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final Class<T> type, final long tid)
		{
			super(type, tid);
		}

	}

}
