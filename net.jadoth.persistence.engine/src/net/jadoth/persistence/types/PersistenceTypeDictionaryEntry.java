package net.jadoth.persistence.types;

import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;

/**
 * Data that ties a {@link PersistenceTypeDescription} to a biunique type id, aka a {@link SwizzleTypeIdentity}.
 * 
 * @author TM
 *
 */
public interface PersistenceTypeDictionaryEntry extends PersistenceTypeDescription, SwizzleTypeIdentity
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	@Override
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
