package one.microstream.persistence.binary.types;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public interface BinaryLegacyTypeHandler<T> extends PersistenceLegacyTypeHandler<Binary, T>, BinaryTypeHandler<T>
{
	@Override
	public default BinaryLegacyTypeHandler<T> initialize(final long typeId)
	{
		PersistenceLegacyTypeHandler.super.initialize(typeId);
		return this;
	}
		
	@Override
	public default void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		PersistenceLegacyTypeHandler.super.store(data, instance, objectId, handler);
	}
	
	
	
	public abstract class Abstract<T>
	extends PersistenceLegacyTypeHandler.Abstract<Binary, T>
	implements BinaryLegacyTypeHandler<T>
	{
		protected Abstract(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}
		
	}
	
	public abstract class AbstractCustom<T>
	extends AbstractBinaryHandlerCustom<T>
	implements BinaryLegacyTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractCustom(
			final Class<T>                                                    type   ,
			final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
		)
		{
			super(type, members);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized BinaryLegacyTypeHandler.AbstractCustom<T> initialize(final long typeId)
		{
			super.initialize(typeId);
			return this;
		}
		
		@Override
		public void store(
			final Binary                          data    ,
			final T                               instance,
			final long                            objectId,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			BinaryLegacyTypeHandler.super.store(data, instance, objectId, handler);
		}
		
	}
	
}
