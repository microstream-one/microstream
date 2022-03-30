package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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
	 * @param other the description to compare to
	 * @return if this and the other description are equal
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
	 * @param other the description to compare to
	 * @return if this and the other description's structure are equal
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
	 * @param m1 the first member
	 * @param m2 the second member
	 * @return if the two members have the same structure
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
	 * @return if this member directly is a reference.
	 *
	 */
	public boolean isReference();

	/**
	 * @return if this member is primitive value.
	 *
	 */
	public boolean isPrimitive();

	/**
	 * @return if this member is a primitive type definition instead of a field definition.
	 *
	 */
	public boolean isPrimitiveDefinition();

	/**
	 * @return if this member is a enum constant name definition instead of an isntance field definition.
	 */
	public boolean isEnumConstant();

	/**
	 * @return if this field contains references. Either because it is a reference itself,
	 * see {@link #isReference()}, or because it is a complex type that contains one or more
	 * nested members that have references.
	 *
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
