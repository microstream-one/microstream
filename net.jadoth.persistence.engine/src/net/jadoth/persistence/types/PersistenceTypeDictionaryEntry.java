package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDictionaryEntry extends SwizzleTypeIdentity
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	/* (30.06.2015 TM)TODO: PersistenceTypeDescription<?> Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (05.04.2017 TM)NOTE: but does it really have to be stored here?
	 * Wouldn't it be enough to store it in the member description?
	 * E.g. Type "Lazy" PLUS type parameter "[full qualified] Person"
	 */

	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();

	
	
	public static boolean isEqual(
		final PersistenceTypeDictionaryEntry type1,
		final PersistenceTypeDictionaryEntry type2
	)
	{
		return type1 == type2
			|| type1 != null && type2 != null
			&& SwizzleTypeIdentity.equals(type1, type2)
			&& PersistenceTypeDescriptionMember.equalMembers(
				type1.members(),
				type2.members()
			)
		;
	}

	
	
	
	public abstract class AbstractImplementation implements PersistenceTypeDictionaryEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation()
		{
			super();
		}

		@Override
		public final String toString()
		{
			final VarString vc = VarString.New();
			
			vc.add(this.typeId()).blank().add(this.typeName()).blank().add('{');
			if(!this.members().isEmpty())
			{
				vc.lf();
				for(final PersistenceTypeDescriptionMember member : this.members())
				{
					vc.tab().add(member).add(';').lf();
				}
			}
			vc.add('}');
			
			return vc.toString();
		}

	}
	
}
