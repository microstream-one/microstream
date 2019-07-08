package one.microstream.persistence.types;

import java.util.Iterator;

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
	 * 
	 * @return
	 */
	public default String qualifier()
	{
		return null;
	}

	/**
	 * The simple or "pimary" name of the member. E.g. "lastName".
	 * 
	 * @return the member's simple name.
	 */
	public String name();
	
	/**
	 * The type-wide unique (or identifying or full qualified) name of the member.<br>
	 * E.g. "com.my.app.entities.Person#lastname"
	 * 
	 * @return the member's uniquely identifying name.
	 */
	public default String uniqueName()
	{
		// should be the same as the simple name. With the exception of ambiguities via inheritance.
		return this.name();
	}
	
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		return equalDescription(this, other);
	}
	
	/**
	 * Tests whether the passed  {@link PersistenceTypeDescriptionMember} have the same "intended" structure, meaning
	 * same order of fields with same simple name (PersistenceTypeDescriptionMember{@link #name()}) and type name. <br>
	 * For example:<br>
	 * A {@link PersistenceTypeDescriptionMemberField} and a {@link PersistenceTypeDescriptionMemberPseudoField}
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
		return m1 == m2 || m1 != null && m2 != null
			&& m1.typeName().equals(m2.typeName())
			&& m1.name().equals(m2.name())
		;
	}
	
	// (05.07.2019 TM)FIXME: MS-156: consolidate weirdly redundant Description comparisons
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		return m1 == m2 || m1 != null && m2 != null
			&& m1.equalsDescription(m2)
		;
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
	 * Determines if this member is a pseudo field defining a primitive type
	 *
	 * @return
	 */
	public boolean isPrimitiveDefinition();

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
			&& m1.uniqueName().equals(m2.uniqueName())
		;
	}
	
	public static int identityHash(final PersistenceTypeDescriptionMember member)
	{
		return member == null
			? 0
			: member.uniqueName().hashCode()
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
	
	public static boolean equalStructure(
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


	public abstract class Abstract implements PersistenceTypeDescriptionMember
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String   typeName               ;
		private final String   qualifier              ;
		private final String   name                   ;
		private final boolean  isReference            ;
		private final boolean  isPrimitive            ;
		private final boolean  isPrimitiveDefinition  ;
		private final boolean  hasReferences          ;
		private final long     persistentMinimumLength;
		private final long     persistentMaximumLength;
		private final String   qualifiedFieldName     ;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final String   typeName               ,
			final String   qualifier              ,
			final String   name                   ,
			final boolean  isReference            ,
			final boolean  isPrimitive            ,
			final boolean  isPrimitiveDefinition  ,
			final boolean  hasReferences          ,
			final long     persistentMinimumLength,
			final long     persistentMaximumLength
		)
		{
			super();
			this.typeName                = typeName               ;
			this.qualifier               = qualifier              ;
			this.name                    = name                   ;
			this.isReference             = isReference            ;
			this.isPrimitive             = isPrimitive            ;
			this.isPrimitiveDefinition   = isPrimitiveDefinition  ;
			this.hasReferences           = hasReferences          ;
			this.persistentMinimumLength = persistentMinimumLength;
			this.persistentMaximumLength = persistentMaximumLength;
			this.qualifiedFieldName      = PersistenceTypeDictionary.fullQualifiedFieldName(qualifier, name);
		}




		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public final String qualifier()
		{
			return this.qualifier;
		}

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final boolean isReference()
		{
			return this.isReference;
		}

		@Override
		public final boolean isPrimitive()
		{
			return this.isPrimitive;
		}

		@Override
		public final boolean isPrimitiveDefinition()
		{
			return this.isPrimitiveDefinition;
		}

		@Override
		public boolean hasReferences()
		{
			return this.hasReferences;
		}

		@Override
		public long persistentMinimumLength()
		{
			return this.persistentMinimumLength;
		}

		@Override
		public long persistentMaximumLength()
		{
			return this.persistentMaximumLength;
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return persistentLength >= this.persistentMinimumLength && persistentLength <= this.persistentMaximumLength;
		}

		@Override
		public void validatePersistentLength(final long persistentLength)
		{
			if(this.isValidPersistentLength(persistentLength))
			{
				return;
			}
			// (02.05.2014)EXCP: proper exception
			throw new RuntimeException(
				"Invalid persistent length: " + persistentLength
				+ " not in [" + this.persistentMinimumLength + ";" + this.persistentMaximumLength + "]"
			);
		}
		
		@Override
		public String toString()
		{
			return this.typeName() + ' ' + this.uniqueName();
		}

	}

}
