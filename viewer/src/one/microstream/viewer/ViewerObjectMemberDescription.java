package one.microstream.viewer;

import java.lang.reflect.Array;
import java.util.List;

import one.microstream.persistence.binary.types.ViewerException;
import one.microstream.persistence.binary.types.ViewerMemberProvider;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericVariableLength;

/**
 *
 * This class provides access to values and type definitions
 * of elements from the "ViewerObjectDescripton" class.
 *
 */
public abstract class ViewerObjectMemberDescription implements ViewerMemberProvider
{

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	protected final PersistenceTypeDefinitionMember typeDefinition;
	protected final Object value;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected ViewerObjectMemberDescription(final PersistenceTypeDefinitionMember typeDefinition, final Object value)
	{
		this.typeDefinition = typeDefinition;
		this.value = value;
	}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ViewerObjectMemberDescription New(final PersistenceTypeDefinitionMember at, final Object object)
	{
		if(object.getClass().isArray())
		{
			if(Array.getLength(object) > 0)
			{
				if(Array.get(object, 0).getClass().isArray())
				{
					return new ViewerObjectMemberList(at, object);
				}
				else if(at instanceof PersistenceTypeDescriptionMemberFieldGenericComplex)
				{
					return new ViewerObjectMemberComplexList(at, object);
				}
				else if(at instanceof PersistenceTypeDescriptionMemberFieldGenericVariableLength)
				{
					return new ViewerObjectMemberList(at, object);
				}

			}
		}
		else if(at.isReference())
		{
			return new ViewerObjectReference(at, object);
		}
		else
		{
			return new ViewerObjectMemberSimple(at, object);
		}

		throw new RuntimeException("Unsuportet TypeDefinition");
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * The simple or "primary" name of the member, if applicable. E.g. "lastName".
	 * May be null if not applicable.
	 *
	 * See {@link PersistenceTypeDescriptionMember#name()}
	 *
	 * @return the member's simple name.
	 */
	public String getName()
	{
		return this.typeDefinition.name();
	}

	/**
	 *
	 * See {@link PersistenceTypeDescriptionMember#typeName()}
	 *
	 * @return
	 */
	public String getTypeName()
	{
		return this.typeDefinition.typeName();
	}

	/**
	 * Get the raw value object of this member
	 *
	 * @return
	 * value as object
	 */
	public Object getValue()
	{
		return this.value;
	}

	/**
	 * Get the PersistenceTypeDefinitionMember
	 *
	 * @return PersistenceTypeDefinitionMember
	 */
	public PersistenceTypeDefinitionMember getTypeDescription()
	{
		return this.typeDefinition;
	}

	/**
	 * Test if the value of this member is a reference to an other
	 * storage object
	 *
	 * @return
	 * true if this member is a reference, otherwise false
	 */
	public boolean isReference()
	{
		return this.typeDefinition.isReference();
	}

	/**
	 * Get the number of available sub members
	 *
	 * @return
	 */
	public int getMembersCount()
	{
		if(this.value.getClass().isArray())
		{
			return Array.getLength(this.value);
		}

		return 0;
	}

	@Override
	public List<ViewerObjectMemberDescription> getMembers(final int offset, final int count)
	{
		try
		{
			final List<ViewerObjectMemberDescription> allMembers = this.getMembers();
			return allMembers.subList(offset, Math.min(offset + count, allMembers.size()));
		}
		catch(final IndexOutOfBoundsException | IllegalArgumentException e)
		{
			throw new ViewerException("no member for offset " + offset + " count " + count);
		}
	}
}
