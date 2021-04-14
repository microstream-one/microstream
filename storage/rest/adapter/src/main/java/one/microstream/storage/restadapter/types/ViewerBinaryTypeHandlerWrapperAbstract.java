package one.microstream.storage.restadapter.types;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;

public abstract class ViewerBinaryTypeHandlerWrapperAbstract<T> implements PersistenceTypeHandler<Binary, Object>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	protected final PersistenceTypeHandler<Binary, T> nativeHandler;
	protected final ViewerBinaryTypeHandlerGeneric genericHandler;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerWrapperAbstract(final PersistenceTypeHandler<Binary, T> nativeHandler)
	{
		super();
		this.nativeHandler = nativeHandler;
		this.genericHandler = null;
	}

	public ViewerBinaryTypeHandlerWrapperAbstract(final PersistenceTypeHandler<Binary, T> nativeHandler,
			final ViewerBinaryTypeHandlerGeneric genericHandler)
	{
		super();
		this.nativeHandler = nativeHandler;
		this.genericHandler = genericHandler;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public long typeId() {
		return this.nativeHandler.typeId();
	}

	@Override
	public String typeName() {
		return this.nativeHandler.typeName();
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return this.nativeHandler.hasPersistedReferences();
	}

	@Override
	public long membersPersistedLengthMinimum()
	{
		return this.nativeHandler.membersPersistedLengthMinimum();
	}

	@Override
	public long membersPersistedLengthMaximum()
	{
		return this.nativeHandler.membersPersistedLengthMaximum();
	}

	@Override
	public boolean isPrimitiveType()
	{
		return this.nativeHandler.isPrimitiveType();
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return this.nativeHandler.hasVaryingPersistedLengthInstances();
	}

	@Override
	public Class<Object> type()
	{
		return Object.class;
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.nativeHandler.allMembers();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		return this.nativeHandler.instanceMembers();
	}

	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.nativeHandler.iterateMemberTypes(logic);
	}

	@Override
	public void iterateInstanceReferences(final Object instance, final PersistenceFunction iterator)
	{
		//do nothing
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Object                          instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		//do nothing
	}

	@Override
	public void updateState(final Binary medium, final Object instance, final PersistenceLoadHandler handler)
	{
		//do nothing
	}

	@Override
	public void complete(final Binary medium, final Object instance, final PersistenceLoadHandler handler)
	{
		//do nothing
	}

	@Override
	public void iterateLoadableReferences(final Binary medium, final PersistenceReferenceLoader iterator)
	{
		//do nothing
	}

	@Override
	public PersistenceTypeHandler<Binary, Object> initialize(final long typeId)
	{
		return this;
	}

	@Override
	public Class<Binary> dataType()
	{
		throw new UnsupportedOperationException();
	}

}
