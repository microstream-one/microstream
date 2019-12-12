package one.microstream.persistence.binary.types;

import java.util.ArrayList;
import java.util.List;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.viewer.ViewerObjectMemberDescription;

/**
 * This class encapsulates the type definition and all field values retrieved
 * for an object received from the ViewerPersistenceManager.
 *
 */
public class ViewerObjectDescription implements ViewerMemberProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long ObjectId;
	private Object[] values;
	private Object primitiveInstance;
	private PersistenceTypeDefinition persistenceTypeDefinition;
	private ViewerObjectMemberDescription parent;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerObjectDescription()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////


	/**
	 * Get the microstream object id for this object
	 *
	 * @return object id as long
	 */
	public long getObjectId()
	{
		return this.ObjectId;
	}

	public void setObjectId(final long objectId)
	{
		this.ObjectId = objectId;
	}

	public Object[] getValues()
	{
		return this.values;
	}

	public void setValues(final Object[] values)
	{
		this.values = values;
	}

	/**
	 * Get the assigned primitive value instance or null if it is not available
	 *
	 * @return
	 */
	public Object getPrimitiveInstance()
	{
		return this.primitiveInstance;
	}

	public void setPrimitiveInstance(final Object primitiveInstance)
	{
		this.primitiveInstance = primitiveInstance;
	}

	public PersistenceTypeDefinition getPersistenceTypeDefinition()
	{
		return this.persistenceTypeDefinition;
	}

	public void setPersistenceTypeDefinition(final PersistenceTypeDefinition persistenceTypeDefinition)
	{
		this.persistenceTypeDefinition = persistenceTypeDefinition;
	}

	/**
	 * Check if an primitive value instance is assigned to this object
	 *
	 * @return true if the object has a primitive value instance
	 */
	public boolean hasPrimitiveObjectInstance()
	{
		return this.primitiveInstance != null;
	}

	/**
	 * Get a list of all members of this object
	 *
	 * @return ViewerObjectMemberDescription array
	 */
	@Override
	public List<ViewerObjectMemberDescription> getMembers()
	{
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> members = this.persistenceTypeDefinition.instanceMembers();

		final int numMembers = members.intSize();
		final List<ViewerObjectMemberDescription> membersDesc = new ArrayList<>();

		for(int i = 0; i <numMembers; i++)
		{
			membersDesc.add(ViewerObjectMemberDescription.New(members.at(i), this.values[i]));
		}

		return membersDesc;
	}

	@Override
	public List<ViewerObjectMemberDescription> getMembers(final int offset, final int count)
	{
		//TODO: handle IndexOutOfBoundsException
		final List<ViewerObjectMemberDescription> allMembers = this.getMembers();
		return allMembers.subList(offset, offset+count);
	}

	public void setParent(final ViewerObjectMemberDescription parent)
	{
		this.parent = parent;
	}

	public ViewerObjectMemberDescription getParent()
	{
		return this.parent;
	}

	@Override
	public ViewerObjectMemberDescription getMember(final int index)
	{
		final PersistenceTypeDefinitionMember member = this.persistenceTypeDefinition.instanceMembers().at(index);
		return ViewerObjectMemberDescription.New(member, this.values[index]);
	}
}
