package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.Iterator;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.hash.HashEqualator;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.util.Equalator;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDescription extends SwizzleTypeIdentity
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	/* (30.06.2015 TM)TODO: PersistenceTypeDescription Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 */

	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();

	public boolean hasReferences();

	public boolean hasVariableLength();

	public boolean isPrimitive();



	public static boolean determineVariableLength(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.isVariableLength())
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

	public static final HashEqualator<PersistenceTypeDescription> EQUAL_TYPE =
		new HashEqualator<PersistenceTypeDescription>()
		{
			@Override
			public int hash(final PersistenceTypeDescription typeDescription)
			{
				return PersistenceTypeDescription.hashCode(typeDescription);
			}

			@Override
			public boolean equal(final PersistenceTypeDescription td1, final PersistenceTypeDescription td2)
			{
				return PersistenceTypeDescription.equalType(td1, td2);
			}
		}
	;



	public static int hashCode(final PersistenceTypeDescription typeDescription)
	{
		return Long.hashCode(typeDescription.typeId()) & typeDescription.typeName().hashCode();
	}

	public static boolean equalType(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		if(td1 == td2)
		{
			return true;
		}
		if(td1 == null)
		{
			return td2 == null;
		}
		if(td2 == null)
		{
			return false;
		}
		return SwizzleTypeIdentity.Static.equals(td1, td2);
	}

	public static boolean equalDescription(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		if(td1 == td2)
		{
			return true;
		}
		if(td1 == null)
		{
			return td2 == null;
		}
		if(td2 == null)
		{
			return false;
		}
		return SwizzleTypeIdentity.Static.equals(td1, td2)
			&& equalMembers(td1, td2, PersistenceTypeDescriptionMember::isEqual)
		;
	}

	public static boolean equalMembers(
		final PersistenceTypeDescription                  td1      ,
		final PersistenceTypeDescription                  td2      ,
		final Equalator<PersistenceTypeDescriptionMember> equalator
	)
	{
		// (01.07.2015 TM)NOTE: must iterate explicitely to guarantee equalator calls (avoid size-based early-aborting)
		final Iterator<? extends PersistenceTypeDescriptionMember> it1 = td1.members().iterator();
		final Iterator<? extends PersistenceTypeDescriptionMember> it2 = td2.members().iterator();

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

	
	
	public static <T> PersistenceTypeDescription New(
		final long                                                         typeId  ,
		final String                                                       typeName,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return new PersistenceTypeDescription.Implementation<>(typeId, typeName, members);
	}



	public final class Implementation<T> implements PersistenceTypeDescription
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long                                                          typeId        ;
		final String                                                        typeName      ;
		final XImmutableSequence<? extends PersistenceTypeDescriptionMember> members       ;
		final boolean                                                        hasReferences ;
		final boolean                                                        isPrimitive   ;
		final boolean                                                        variableLength;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final long                                                         typeId  ,
			final String                                                       typeName,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			super();
			this.typeId         = typeId           ;
			this.typeName       = notNull(typeName);
			this.members        = members.immure() ; // same instance if already immutable
			this.hasReferences  = PersistenceTypeDescriptionMember.determineHasReferences (members);
			this.isPrimitive    = PersistenceTypeDescription      .determineIsPrimitive   (members);
			this.variableLength = PersistenceTypeDescription      .determineVariableLength(members);
		}

		

		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final long typeId()
		{
			return this.typeId;
		}
		
		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public final XImmutableSequence<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.members;
		}

		@Override
		public final boolean hasReferences()
		{
			return this.hasReferences;
		}

		@Override
		public final boolean isPrimitive()
		{
			return this.isPrimitive;
		}

		@Override
		public final boolean hasVariableLength()
		{
			return this.variableLength;
		}

		@Override
		public final String toString()
		{
			final VarString vc = VarString.New();
			
			vc.add(this.typeId).blank().add(this.typeName).blank().add('{');
			if(!this.members.isEmpty())
			{
				vc.lf();
				for(final PersistenceTypeDescriptionMember member : this.members)
				{
					vc.tab().add(member).add(';').lf();
				}
			}
			vc.add('}');
			
			return vc.toString();
		}

	}

}
