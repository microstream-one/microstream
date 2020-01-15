package one.microstream.persistence.binary.types;

import java.util.ArrayList;
import java.util.List;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.meta.XDebug;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.viewer.EmbeddedStorageRestAdapter;
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
	private long length;
	private ViewerObjectDescription[] references;

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

	public long getLength()
	{
		return this.length;
	}

	public void setLength(final long variableSize)
	{
		this.length = variableSize;
	}

	public ViewerObjectDescription[] getReferences()
	{
		return this.references;
	}

	public void setReferences(final ViewerObjectDescription[] references)
	{
		this.references = references;
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
		try
		{
			final PersistenceTypeDefinitionMember member = this.persistenceTypeDefinition.instanceMembers().at(index);
			return ViewerObjectMemberDescription.New(member, this.values[index]);
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new ViewerException("no member for index " + index);
		}
	}

	public void resolveReferences(final long referenceOffset, final long referenceLength, final EmbeddedStorageRestAdapter storageRestAdapter)
	{
		int referenceIndex = 0;
		int referenceCount = 0;

		final List<ViewerObjectDescription> resolvedReferences = new ArrayList<>();

		for(int i = 0; i < this.values.length; i++)
		{
			if(this.values[i] instanceof ViewerObjectReferenceWrapper)
			{
				if(referenceIndex++ < referenceOffset) continue;
				if(referenceCount++ >= referenceLength) break;

				final long oid = ((ViewerObjectReferenceWrapper) this.values[i]).getObjectId();
				if(oid > 0)
				{
					resolvedReferences.add(storageRestAdapter.getStorageObject(oid));
				}
				else
				{
					resolvedReferences.add(null);
				}
			}
		}

		this.references = resolvedReferences.toArray(new ViewerObjectDescription[0]);

		XDebug.println("");
	}
}
