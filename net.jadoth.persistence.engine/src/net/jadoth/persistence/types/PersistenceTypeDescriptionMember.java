package net.jadoth.persistence.types;

import java.util.Iterator;
import java.util.function.Consumer;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.equality.Equalator;


public interface PersistenceTypeDescriptionMember
{
	public String typeName();

	/**
	 * The direct, simple name of the member. E.g. "lastName".
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
	
	public default String declaringTypeName()
	{
		// should be the same as the type name. With the exception of inheritance.
		return this.typeName();
	}

	public boolean equals(PersistenceTypeDescriptionMember m2, DescriptionMemberEqualator equalator);

	public void assembleTypeDescription(PersistenceTypeDescriptionMember.Appender assembler);

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
	
	public static boolean equalMembers(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2
	)
	{
		return equalMembers(members1, members2, PersistenceTypeDescriptionMember::isEqual);
	}
	
	public static boolean equalMembers(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1 ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2 ,
		final Equalator<PersistenceTypeDescriptionMember>                  equalator
	)
	{
		// (01.07.2015 TM)NOTE: must iterate explicitely to guarantee equalator calls (avoid size-based early-aborting)
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



	public abstract class AbstractImplementation implements PersistenceTypeDescriptionMember
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String  typeName               ;
		private final String  name                   ;
		private final boolean isReference            ;
		private final boolean isPrimitive            ;
		private final boolean isPrimitiveDefinition  ;
		private final boolean hasReferences          ;
		private final long    persistentMinimumLength;
		private final long    persistentMaximumLength;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(
			final String  typeName               ,
			final String  name                   ,
			final boolean isReference            ,
			final boolean isPrimitive            ,
			final boolean isPrimitiveDefinition  ,
			final boolean hasReferences          ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			super();
			this.typeName                = typeName               ;
			this.name                    = name                   ;
			this.isReference             = isReference            ;
			this.isPrimitive             = isPrimitive            ;
			this.isPrimitiveDefinition   = isPrimitiveDefinition  ;
			this.hasReferences           = hasReferences          ;
			this.persistentMinimumLength = persistentMinimumLength;
			this.persistentMaximumLength = persistentMaximumLength;
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

	public interface Appender extends Consumer<PersistenceTypeDescriptionMember>
	{
		public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberField typeMember);

		public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberPseudoFieldSimple typeMember);

		public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberPseudoFieldVariableLength typeMember);

		public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberPseudoFieldComplex typeMember);

		public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberPrimitiveDefinition typeMember);

	}



	public static final DescriptionMemberEqualator DESCRIPTION_MEMBER_EQUALATOR =
		new DescriptionMemberEqualator.Implementation()
	;


	public static boolean isEqual(final PersistenceTypeDescriptionMember m1, final PersistenceTypeDescriptionMember m2)
	{
		// must check for null, e.g. when checking size-mismatched member lists
		return m1 != null && m2 != null && m1.equals(m2, DESCRIPTION_MEMBER_EQUALATOR);
	}



	public interface DescriptionMemberEqualator
	{
		public boolean hasEqualField(PersistenceTypeDescriptionMemberField m1, PersistenceTypeDescriptionMemberField m2);

		public boolean equals(PersistenceTypeDescriptionMemberField m1, PersistenceTypeDescriptionMember m2);

		public boolean equals(PersistenceTypeDescriptionMemberPseudoFieldSimple m1, PersistenceTypeDescriptionMember m2);

		public boolean equals(PersistenceTypeDescriptionMemberPseudoFieldVariableLength m1, PersistenceTypeDescriptionMember m2);

		public boolean equals(PersistenceTypeDescriptionMemberPseudoFieldComplex m1, PersistenceTypeDescriptionMember m2);

		public boolean equals(PersistenceTypeDescriptionMemberPrimitiveDefinition m1, PersistenceTypeDescriptionMember m2);



		public final class Implementation implements DescriptionMemberEqualator
		{
			private static boolean equalTypeAndName(
				final PersistenceTypeDescriptionMember m1,
				final PersistenceTypeDescriptionMember m2
			)
			{
				return m1.typeName().equals(m2.typeName()) && m1.name().equals(m2.name());
			}

			private static boolean equalFields(
				final PersistenceTypeDescriptionMemberField m1,
				final PersistenceTypeDescriptionMemberField m2
			)
			{
				return equalTypeAndName(m1, m2) && m1.declaringTypeName().equals(m2.declaringTypeName());
			}



			@Override
			public boolean hasEqualField(
				final PersistenceTypeDescriptionMemberField m1,
				final PersistenceTypeDescriptionMemberField m2
			)
			{
				return equalFields(m1, m2);
			}

			@Override
			public boolean equals(
				final PersistenceTypeDescriptionMemberField m1,
				final PersistenceTypeDescriptionMember      m2
			)
			{
				return m2 instanceof PersistenceTypeDescriptionMemberField
					&& equalFields(m1, (PersistenceTypeDescriptionMemberField)m2)
				;
			}

			@Override
			public boolean equals(
				final PersistenceTypeDescriptionMemberPseudoFieldSimple m1,
				final PersistenceTypeDescriptionMember                  m2
			)
			{
				return m2 instanceof PersistenceTypeDescriptionMemberPseudoFieldSimple
					&& equalTypeAndName(m1, m2);
			}

			@Override
			public boolean equals(
				final PersistenceTypeDescriptionMemberPseudoFieldVariableLength m1,
				final PersistenceTypeDescriptionMember                          m2
			)
			{
				return m2 instanceof PersistenceTypeDescriptionMemberPseudoFieldVariableLength
					&& equalTypeAndName(m1, m2)
				;
			}

			@Override
			public boolean equals(
				final PersistenceTypeDescriptionMemberPseudoFieldComplex m1,
				final PersistenceTypeDescriptionMember                   m2
			)
			{
				return m2 instanceof PersistenceTypeDescriptionMemberPseudoFieldComplex
					&& equalTypeAndName(m1, m2)
					&& m1.members().equalsContent(
						((PersistenceTypeDescriptionMemberPseudoFieldComplex)m2).members(),
						PersistenceTypeDescriptionMember::isEqual
					)
				;
			}

			@Override
			public boolean equals(
				final PersistenceTypeDescriptionMemberPrimitiveDefinition m1,
				final PersistenceTypeDescriptionMember                    m2
			)
			{
				/* this method is just for completeness as it should never be called anyway
				 * (primitive definition members get substituted by a special primitive type definition type)
				 */
				return m2 instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
					&& m1.primitiveDefinition().equals(
						((PersistenceTypeDescriptionMemberPrimitiveDefinition)m2).primitiveDefinition()
					)
				;
			}

		}

	}

}
