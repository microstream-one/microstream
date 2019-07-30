package one.microstream.persistence.types;

import java.util.Iterator;
import java.util.Objects;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.equality.Equalator;
import one.microstream.hashing.HashEqualator;
import one.microstream.math.XMath;


public interface PersistenceTypeDescriptionMember
{
	public String typeName();
	
	/**
	 * A type-internal qualifier to distinct different members with equal "primary" name. E.g. reflection-based
	 * type handling where fields names are only unique in combination with their declaring class.
	 * <p>
	 * May be {@code null} if not applicable.
	 * 
	 * @return the member's qualifier string to ensure a unique {@link #identifier()} in a group of members.
	 */
	public String qualifier();
	
	/**
	 * The name of the member identifying it in its parent group of members.<br>
	 * E.g. "com.my.app.entities.Person#lastname".
	 * <p>
	 * May never be {@code null}.
	 * 
	 * @return the member's uniquely identifying name.
	 */
	public String identifier();
	
	/**
	 * The simple or "primary" name of the member, if applicable. E.g. "lastName".
	 * <p>
	 * May be {@code null} if not applicable.
	 * 
	 * @return the member's simple name.
	 */
	public String name();
	
	public boolean isInstanceMember();
		
	/**
	 * {@link #equalsStructure(PersistenceTypeDescriptionMember)} plus {@link #qualifier()} equality,
	 * to check if a member is really content-wise equal.
	 * 
	 * @param other
	 * @return
	 * 
	 * @see #equalsStructure(PersistenceTypeDescriptionMember)
	 */
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		// calls #equalsStructure to include inheritance overrides. Important!
		return this.equalsStructure(other) && other != null
			&& Objects.equals(this.qualifier(), other.qualifier())
		;
	}

	/**
	 * Structure means equal order of members by type name and simple name.<br>
	 * Not qualifier, since that is only required for intra-type field identification
	 * 
	 * @param other
	 * @return
	 * 
	 * @see #equalDescription(PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember)
	 */
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return equalTypeAndName(this, other);
	}
	
	public static boolean equalTypeAndName(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// attribute checks must be null-safe equals because of primitive definitions
		return m1 == m2 || m2 != null
			&& Objects.equals(m1.typeName(), m2.typeName())
			&& Objects.equals(m1.name()    , m2.name()    )
		;
	}
	
	public static boolean equalTypeAndNameAndQualifier(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// attribute checks must be null-safe equals because of primitive definitions
		return equalTypeAndName(m1, m2) && m2 != null
			&& Objects.equals(m1.qualifier(), m2.qualifier())
		;
	}
	
	/**
	 * Tests whether the passed  {@link PersistenceTypeDescriptionMember} have the same "intended" structure, meaning
	 * same order of fields with same simple name (PersistenceTypeDescriptionMember{@link #name()}) and type name. <br>
	 * For example:<br>
	 * A {@link PersistenceTypeDescriptionMemberFieldReflective} and a {@link PersistenceTypeDescriptionMemberFieldGeneric}
	 * with different member qualifiers are still considered equal.<br>
	 * This is necessary for legacy type mapping to being able to write a custom legacy type handler that is
	 * compatible with a generic type handler derived from reflective information.
	 * 
	 * @param m1
	 * @param m2
	 * @return
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// must delegate to the implementation since complex fields must deep-check their nested fields
		return m1 == m2 || m1 != null && m1.equalsStructure(m2);
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// must delegate to the implementation since complex fields must deep-check their nested fields
		return m1 == m2 || m1 != null && m1.equalsDescription(m2);
	}
	
	
	
	public static long calculatePersistentMinimumLength(
		final long                                                 startValue,
		final Iterable<? extends PersistenceTypeDescriptionMember> members
	)
	{
		long length = startValue;
		for(final PersistenceTypeDescriptionMember member : members)
		{
			length = XMath.addCapped(length, member.persistentMinimumLength());
		}
		
		return length;
	}
	
	public static long calculatePersistentMaximumLength(
		final long                                                 startValue,
		final Iterable<? extends PersistenceTypeDescriptionMember> members
	)
	{
		long length = startValue;
		for(final PersistenceTypeDescriptionMember member : members)
		{
			length = XMath.addCapped(length, member.persistentMaximumLength());
		}
		
		return length;
	}
	

	public void assembleTypeDescription(PersistenceTypeDescriptionMemberAppender assembler);

	/**
	 * Determines if this member directly is a reference.
	 *
	 * @return
	 */
	public boolean isReference();

	/**
	 * Determines if this member is primitive value.
	 *
	 * @return
	 */
	public boolean isPrimitive();

	/**
	 * Determines if this member is a primitive type definition instead of a field definition.
	 *
	 * @return
	 */
	public boolean isPrimitiveDefinition();

	/**
	 * Determines if this member is a enum constant name definition instead of an isntance field definition.
	 *
	 * @return
	 */
	public boolean isEnumConstant();

	/**
	 * Determines if this field contains references. Either because it is a reference itself,
	 * see {@link #isReference()},
	 * or because it is a complex type that contains one or more nested members that have references.
	 *
	 * @return
	 */
	public boolean hasReferences();

	public default boolean isVariableLength()
	{
		return this.persistentMinimumLength() != this.persistentMaximumLength();
	}

	public default boolean isFixedLength()
	{
		return this.persistentMinimumLength() == this.persistentMaximumLength();
	}

	/**
	 * Returns the lowest possible length value that a member of the persistent form for values of the type
	 * represented by this instance can have.
	 * The precise meaning of the length value depends on the actual persistence form.
	 *
	 * @return the persistent form length of null if variable length.
	 * @see #persistentMaximumLength()
	 */
	public long persistentMinimumLength();

	/**
	 * Returns the highest possible length value that a member of the persistent form for values of the type
	 * represented by this instance can have.
	 * The precise meaning of the length value depends on the actual persistence form.
	 *
	 * @return the persistent form length of null if variable length.
	 * @see #persistentMinimumLength()
	 */
	public long persistentMaximumLength();

	public boolean isValidPersistentLength(long persistentLength);

	public void validatePersistentLength(long persistentLength);
	
	public default boolean isIdentical(final PersistenceTypeDescriptionMember other)
	{
		return isIdentical(this, other);
	}

	public static boolean isIdentical(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		return m1 == m2 || m1 != null && m2 != null
			&& m1.identifier().equals(m2.identifier())
		;
	}
	
	public static int identityHash(final PersistenceTypeDescriptionMember member)
	{
		return member == null
			? 0
			: member.identifier().hashCode()
		;
	}
	
	public static IdentityHashEqualator identityHashEqualator()
	{
		return IdentityHashEqualator.SINGLETON;
	}
	
	public final class IdentityHashEqualator implements HashEqualator<PersistenceTypeDescriptionMember>
	{
		static final PersistenceTypeDescriptionMember.IdentityHashEqualator SINGLETON =
			new PersistenceTypeDescriptionMember.IdentityHashEqualator(
		);

		@Override
		public final int hash(final PersistenceTypeDescriptionMember member)
		{
			return identityHash(member);
		}

		@Override
		public final boolean equal(
			final PersistenceTypeDescriptionMember m1,
			final PersistenceTypeDescriptionMember m2
		)
		{
			return isIdentical(m1, m2);
		}
		
	}
		
	public static boolean determineHasReferences(final Iterable<? extends PersistenceTypeDescriptionMember> members)
	{
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.hasReferences())
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean determineIsPrimitive(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return members.size() == 1 && members.get().isPrimitiveDefinition();
	}
	
	public static boolean equalDescriptions(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2
	)
	{
		return equalMembers(members1, members2, PersistenceTypeDescriptionMember::equalDescription);
	}
	
	public static boolean equalStructures(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2
	)
	{
		return equalMembers(members1, members2, PersistenceTypeDescriptionMember::equalStructure);
	}
	
	public static boolean equalMembers(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1 ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2 ,
		final Equalator<PersistenceTypeDescriptionMember>                  equalator
	)
	{
		// (01.07.2015 TM)NOTE: must iterate explicitly to guarantee equalator calls (avoid size-based early-aborting)
		final Iterator<? extends PersistenceTypeDescriptionMember> it1 = members1.iterator();
		final Iterator<? extends PersistenceTypeDescriptionMember> it2 = members2.iterator();

		// intentionally OR to give equalator a chance to handle size mismatches as well (indicated by null)
		while(it1.hasNext() || it2.hasNext())
		{
			final PersistenceTypeDescriptionMember member1 = it1.hasNext() ? it1.next() : null;
			final PersistenceTypeDescriptionMember member2 = it2.hasNext() ? it2.next() : null;

			if(!equalator.equal(member1, member2))
			{
				return false;
			}
		}

		// neither member-member mismatch nor size mismatch, so members must be in order and equal
		return true;
	}

	
	public PersistenceTypeDefinitionMember createDefinitionMember(PersistenceTypeDefinitionMemberCreator creator);

}
