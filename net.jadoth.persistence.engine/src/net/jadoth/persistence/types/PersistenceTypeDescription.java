package net.jadoth.persistence.types;

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
		return typeId + typeIdentifierSeparator() + typeName;
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
	
}
