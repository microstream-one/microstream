package one.microstream.persistence.binary.types;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class ViewerBinaryTypeHandlerGeneric implements PersistenceTypeHandler<Binary, ViewerObjectDescription>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ValueReader[] readers;
	private final PersistenceTypeDefinition persitenceTypeDefinition;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerGeneric(final PersistenceTypeDefinition persistenceTypeDef)
	{
		super();

		this.persitenceTypeDefinition = persistenceTypeDef;
		this.readers = ValueReader.deriveValueReaders(persistenceTypeDef);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public PersistenceTypeDefinition getPersitenceTypeDefinition()
	{
		return this.persitenceTypeDefinition;
	}

	@Override
	public long typeId()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String typeName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPersistedReferences()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long membersPersistedLengthMinimum()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long membersPersistedLengthMaximum()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPrimitiveType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<ViewerObjectDescription> type()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasInstanceReferences()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateInstanceReferences(final ViewerObjectDescription instance, final PersistenceFunction iterator)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void iterateLoadableReferences(final Binary medium, final PersistenceReferenceLoader iterator)
	{
		//do nothing
	}

	@Override
	public void store(final Binary medium, final ViewerObjectDescription instance, final long objectId, final PersistenceStoreHandler handler)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ViewerObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ViewerObjectDescription objectDescription = new ViewerObjectDescription();
		objectDescription.setObjectId(medium.getBuildItemObjectId());
		objectDescription.setPersistenceTypeDefinition(this.persitenceTypeDefinition);
		objectDescription.setValues(ValueReader.readObjectValues(medium, this.readers, null));

		return	objectDescription;
	}

	@Override
	public void updateState(final Binary medium, final ViewerObjectDescription instance, final PersistenceLoadHandler handler)
	{
		//do nothing
	}

	@Override
	public void complete(final Binary medium, final ViewerObjectDescription instance, final PersistenceLoadHandler handler)
	{
		//do nothing
	}

	@Override
	public PersistenceTypeHandler<Binary, ViewerObjectDescription> initialize(final long typeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		throw new UnsupportedOperationException();
	}
}
