package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;

/**
 * Data that describes the persistence-relevant aspects of a type, meaning its full type name and all its
 * persistable members (fields).
 * 
 * @author TM
 *
 */
public interface PersistenceTypeDescription extends SwizzleTypeIdentity
{
	@Override
	public String typeName();
	
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();
	
	/* (30.06.2015 TM)TODO: PersistenceTypeDescription Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (13.09.2018 TM)NOTE: both here and in the member description
	 */
	
	
	public static char typeIdentifierSeparator()
	{
		return ':';
	}
	
	public static String buildTypeIdentifier(final long typeId, final String typeName)
	{
		// simple string concatenation syntax messes up the char adding.
		return VarString.New(100).add(typeId).add(typeIdentifierSeparator()).add(typeName).toString();
	}
	
	public static String buildTypeIdentifier(final PersistenceTypeDescription typeDescription)
	{
		return buildTypeIdentifier(typeDescription.typeId(), typeDescription.typeName());
	}
	
	public default String toTypeIdentifier()
	{
		return buildTypeIdentifier(this);
	}
 	
	/**
	 * Equal content description, without TypeId comparison
	 * 
	 * @param td1
	 * @param td2
	 * @return
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeName().equals(td1.typeName())
			&& PersistenceTypeDescriptionMember.equalDescriptions(td1.members(), td2.members())
		;
	}
	
	
	public static PersistenceTypeDescription Identity(final long typeId, final String typeName)
	{
		return new PersistenceTypeDescription.IdentityImplementation(typeId, typeName);
	}
	
	public final class IdentityImplementation implements PersistenceTypeDescription
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long   typeId  ;
		private final String typeName;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		IdentityImplementation(final long typeId, final String typeName)
		{
			super();
			this.typeId   = typeId  ;
			this.typeName = typeName;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> members()
		{
			return X.empty();
		}
		
	}
	
}
