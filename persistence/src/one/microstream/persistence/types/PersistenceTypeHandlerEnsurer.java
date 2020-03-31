package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

/**
 * Named "ensurer" because depending on the case, it creates a new type handler or it just returns
 * already existing, pre-registered ones. So "ensuring" is the most fitting common denominator.
 * 
 * @author TM
 */
public interface PersistenceTypeHandlerEnsurer<D>
extends PersistenceTypeHandlerIterable<D>, PersistenceDataTypeHolder<D>
{
	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;
	
	
	
	public static <D> PersistenceTypeHandlerEnsurer.Default<D> New(
		final Class<D>                                dataType                 ,
		final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry,
		final PersistenceTypeHandlerCreator<D>        typeHandlerCreator
	)
	{
		return new PersistenceTypeHandlerEnsurer.Default<>(
			notNull(dataType),
			notNull(customTypeHandlerRegistry),
			notNull(typeHandlerCreator)
		);
	}

	public class Default<D> extends PersistenceDataTypeHolder.Default<D> implements PersistenceTypeHandlerEnsurer<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry;
		final PersistenceTypeHandlerCreator<D>        typeHandlerCreator       ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Class<D>                                dataType                 ,
			final PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry,
			final PersistenceTypeHandlerCreator<D>        typeHandlerCreator
		)
		{
			super(dataType);
			this.customTypeHandlerRegistry = customTypeHandlerRegistry;
			this.typeHandlerCreator        = typeHandlerCreator       ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			final PersistenceTypeHandler<D, T> providedHandler;
			try
			{
				providedHandler = Persistence.searchProvidedTypeHandler(this.dataType(), type, null);
			}
			catch(final ReflectiveOperationException e)
			{
				throw new PersistenceException(e);
			}
			if(providedHandler != null)
			{
				return providedHandler;
			}
			
			// lookup predefined handler first to cover primitives and to give custom handlers precedence
			final PersistenceTypeHandler<D, ? super T> customHandler = this.customTypeHandlerRegistry.lookupTypeHandler(type);
			if(customHandler != null)
			{
				return customHandler;
			}
			
			return this.typeHandlerCreator.createTypeHandler(type);
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(final C iterator)
		{
			return this.customTypeHandlerRegistry.iterateTypeHandlers(iterator);
		}
		
		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(
			final C iterator
		)
		{
			return this.customTypeHandlerRegistry.iterateLegacyTypeHandlers(iterator);
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
		{
			return this.customTypeHandlerRegistry.iterateAllTypeHandlers(iterator);
		}

	}

}
