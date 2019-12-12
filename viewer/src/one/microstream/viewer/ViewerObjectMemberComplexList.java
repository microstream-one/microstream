package one.microstream.viewer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

public class ViewerObjectMemberComplexList extends ViewerObjectMemberDescription {

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerObjectMemberComplexList(final PersistenceTypeDefinitionMember typeDefinition, final Object value)
	{
		super(typeDefinition, value);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public List<ViewerObjectMemberDescription> getMembers()
	{
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> m =
		((PersistenceTypeDefinitionMemberFieldGenericComplex) this.typeDefinition).members();

		final int numElements =  Array.getLength(this.value);
		final List<ViewerObjectMemberDescription> membersDesc = new ArrayList<>();

		for(int i = 0; i < numElements; i++)
		{
			final Object mValue = Array.get(this.value, i);

			if(m.size() == numElements)
			{
				membersDesc.add(ViewerObjectMemberDescription.New((PersistenceTypeDefinitionMember)m.at(i), mValue));
			}
			else
			{
				membersDesc.add(ViewerObjectMemberDescription.New((PersistenceTypeDefinitionMember)m.at(0), mValue));
			}
		}

		return membersDesc;
	}

	@Override
	public ViewerObjectMemberDescription getMember(final int index)
	{
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> m =
		((PersistenceTypeDefinitionMemberFieldGenericComplex) this.typeDefinition).members();

		final int numElements =  Array.getLength(this.value);
		final Object mValue = Array.get(this.value, index);

		if(m.size() == numElements)
		{
			return ViewerObjectMemberDescription.New((PersistenceTypeDefinitionMember)m.at(index), mValue);
		}
		else
		{
			return ViewerObjectMemberDescription.New((PersistenceTypeDefinitionMember)m.at(0), mValue);
		}

	}
}
