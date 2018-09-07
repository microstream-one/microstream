package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;

/**
 * Data that describes the persistence-relevant aspects of a type, meaning its full type name and all its
 * persistable members (fields).
 * 
 * @author TM
 *
 */
public interface PersistenceTypeDescription
{
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
	 * (05.04.2017 TM)NOTE: but does it really have to be stored here?
	 * Wouldn't it be enough to store it in the member description?
	 * E.g. Type "Lazy" PLUS type parameter "[full qualified] Person"
	 */
	
	public static boolean isEqualStructure(final PersistenceTypeDescription ts1, final PersistenceTypeDescription ts2)
	{
		return ts1 == ts2
			|| ts1 != null && ts2 != null
			&& ts1.typeName().equals(ts2.typeName())
			&& PersistenceTypeDescriptionMember.equalDescriptions(ts1.members(), ts2.members())
		;
	}
}
