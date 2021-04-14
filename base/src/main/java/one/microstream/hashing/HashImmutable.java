package one.microstream.hashing;

import java.util.Collection;

import one.microstream.equality.Equalator;

/**
 * Marker type to indicate that all fields (the state) used in the implementation of {@link #equals(Object)}
 * and {@link #hashCode()} are immutable (will never change) and thus the results of {@link #equals(Object)}
 * and {@link #hashCode()} can never change during the life span of an instance of this type.
 * <p>
 * This additional contract is required to enable the proper use of {@link Object#equals(Object)} and
 * {@link Object#hashCode()}, because {@link Object#hashCode()}-depending hashing depends on this behavior
 * to remain consistent.
 * <p>
 * For any type not implementing this class (or not commonly known to be immutable, like {@link String},
 * {@link Integer}, etc.), do not rely on neither {@link Object#equals(Object)} nor {@link Object#hashCode()},
 * as implementations overriding them are potentially broken in terms of the required behavior (e.g. JDK
 * implementations of {@link Collection} are).<br>
 * For those cases, use an externally defined {@link Equalator} or {@link HashEqualator} with fitting implementation
 * instead.
 *
 * 
 */
public interface HashImmutable
{
	/**
	 * Marker declaration to indicate that for classes of this type, {@link Object#equals(Object)} can be properly
	 * used.
	 *
	 * @param  other the reference object with which to compare.
	 * @return {@code true} if this object can be treated as the <i>same</i> as <tt>other</tt>,
	 *         {@code false} otherwise.
	 * @see    #hashCode()
	 */
	@Override
	public boolean equals(Object other);

	/**
	 * Marker declaration to indicate that for classes of this type, {@link Object#hashCode()} can be properly
	 * used.
	 *
	 * @return a hash code value for this object corresponding to the implementation of {@link #equals(Object)}
	 * @see    #equals(java.lang.Object)
	 */
	@Override
	public int hashCode();
}
