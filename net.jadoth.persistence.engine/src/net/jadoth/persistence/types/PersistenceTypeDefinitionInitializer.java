package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionInitializer<T> extends PersistenceTypeDescription
{
	@Override
	public String typeName();
	
	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();
	
	public Class<T> type();
		
	public PersistenceTypeDefinition<T> initialize(long typeId);
	
	
	
	
	public static <M, T> PersistenceTypeDefinitionInitializer.Implementation<M, T> New(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final PersistenceTypeHandler<M, T>     typeHandler
	)
	{
		return new PersistenceTypeDefinitionInitializer.Implementation<>(
			notNull(typeHandlerManager),
			notNull(typeHandler)
		);
	}
	
	
	
	public final class Implementation<M, T> implements PersistenceTypeDefinitionInitializer<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeHandlerManager<M> typeHandlerManager;
		final PersistenceTypeHandler<M, T>     typeHandler       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeHandlerManager<M> typeHandlerManager,
			final PersistenceTypeHandler<M, T>     typeHandler
		)
		{
			super();
			this.typeHandlerManager = typeHandlerManager;
			this.typeHandler        = typeHandler       ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String typeName()
		{
			return this.typeHandler.typeName();
		}

		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.typeHandler.members();
		}
		
		@Override
		public final Class<T> type()
		{
			return this.typeHandler.type();
		}

		@Override
		public final PersistenceTypeDefinition<T> initialize(final long typeId)
		{
			synchronized(this.typeHandlerManager)
			{
				this.typeHandlerManager.validateTypeMapping(typeId, this.type());
				this.typeHandler.initializeTypeId(typeId);
				this.typeHandlerManager.register(this.typeHandler);
			}
			return this.typeHandler;
		}
		
	}
	
}
