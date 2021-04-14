package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.entity.Entity;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;
import one.microstream.typing.LambdaTypeRecognizer;

/**
 * Named "ensurer" because depending on the case, it creates a new type handler or it just returns
 * already existing, pre-registered ones. So "ensuring" is the most fitting common denominator.
 * 
 * 
 */
public interface PersistenceTypeHandlerEnsurer<D>
extends PersistenceTypeHandlerIterable<D>, PersistenceDataTypeHolder<D>
{
	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;
	
	
	
	public static <D> PersistenceTypeHandlerEnsurer.Default<D> New(
		final Class<D>                                  dataType                   ,
		final PersistenceCustomTypeHandlerRegistry<D>   customTypeHandlerRegistry  ,
		final PersistenceTypeAnalyzer                   typeAnalyzer               ,
		final LambdaTypeRecognizer                      lambdaTypeRecognizer       ,
		final PersistenceAbstractTypeHandlerSearcher<D> abstractTypeHandlerSearcher,
		final PersistenceTypeHandlerCreator<D>          typeHandlerCreator
	)
	{
		return new PersistenceTypeHandlerEnsurer.Default<>(
			notNull(dataType)                   ,
			notNull(customTypeHandlerRegistry)  ,
			notNull(typeAnalyzer)               ,
			notNull(lambdaTypeRecognizer)       ,
			notNull(abstractTypeHandlerSearcher),
			notNull(typeHandlerCreator)
		);
	}

	public class Default<D> extends PersistenceDataTypeHolder.Default<D> implements PersistenceTypeHandlerEnsurer<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceCustomTypeHandlerRegistry<D>   customTypeHandlerRegistry  ;
		final PersistenceTypeAnalyzer                   typeAnalyzer               ;
		final LambdaTypeRecognizer                      lambdaTypeRecognizer       ;
		final PersistenceAbstractTypeHandlerSearcher<D> abstractTypeHandlerSearcher;
		final PersistenceTypeHandlerCreator<D>          typeHandlerCreator         ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Class<D>                                  dataType                   ,
			final PersistenceCustomTypeHandlerRegistry<D>   customTypeHandlerRegistry  ,
			final PersistenceTypeAnalyzer                   typeAnalyzer               ,
			final LambdaTypeRecognizer                      lambdaTypeRecognizer       ,
			final PersistenceAbstractTypeHandlerSearcher<D> abstractTypeHandlerSearcher,
			final PersistenceTypeHandlerCreator<D>          typeHandlerCreator
		)
		{
			super(dataType);
			this.customTypeHandlerRegistry   = customTypeHandlerRegistry  ;
			this.typeAnalyzer                = typeAnalyzer               ;
			this.lambdaTypeRecognizer        = lambdaTypeRecognizer       ;
			this.abstractTypeHandlerSearcher = abstractTypeHandlerSearcher;
			this.typeHandlerCreator          = typeHandlerCreator         ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			final PersistenceTypeHandler<D, ? super T> providedHandler = this.searchProvidedTypeHandler(type);
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
			
			// should never happen or more precisely: should only happen for unhandled primitive types
			if(type.isPrimitive())
			{
				throw new PersistenceException(
					"Primitive types must be handled by default (dummy) handler implementations."
				);
			}
			
			// class meta data instances special handling
			if(type == Class.class)
			{
				throw new PersistenceException(
					"Persisting " + Class.class.getSimpleName() + " instances requires a special-tailored "
					+ PersistenceTypeHandler.class.getSimpleName()
					+ " and cannot be done in a generic way."
				);
			}
			
			// Do NOT replace this with Proxy#isProxyClass. See rationale inside the XReflect method.
			if(XReflect.isProxyClass(type))
			{
				return this.typeHandlerCreator.createTypeHandlerProxy(type);
			}
			
			// array special casing
			if(type.isArray())
			{
				return this.typeHandlerCreator.createTypeHandlerArray(type);
			}
			
			/* (25.03.2019 TM)NOTE:
			 * Note on lambdas:
			 * There is (currently) no way of determining if an instance is a lambda.
			 * Any checks on the name are best guesses, not reliable logic.
			 * It may work in certain (even most) applications absolutely correctly, but it is not
			 * absolutely reliable to not being ambiguous and hence wrong.
			 * 
			 * Here (https://stackoverflow.com/questions/23870478/how-to-correctly-determine-that-an-object-is-a-lambda),
			 * Brian Goetz babbles some narrow-minded stuff about that it should not matter if an instance is
			 * a lambda or not and that one with the wish to recognize that would "almost certainly" be doing
			 * something wrong.
			 * Cute little world he lives in.
			 * 
			 * On a more competent note:
			 * Persisting a lambda as a stateless entity is actually not technically wrong.
			 * The only problem is that the JVM cannot resolve the type name it itself generated to describe the lambda.
			 * That is simply a shortcoming of the (current) JVM that may get fixed in the future.
			 * (also, it directly proves the good Brian oh so wrong. If the JVM cannot resolve its own lambda type,
			 * as opposed to inner class types etc., there IS a need to recognize lambdas for performing generic
			 * processes like serialization or other reflective analyzing.)
			 * Or it might not, given the displayed level of competence.
			 * 
			 * Until then:
			 * If required, a simple solution would be to register a custom implementation of the modular
			 * LambdaTypeRecognizer that checks for lambdas with whatever logic works in the particular case.
			 */
			
			if(this.lambdaTypeRecognizer.isLambdaType(type))
			{
				return this.typeHandlerCreator.createTypeHandlerLambda(type);
			}
			
			// checked first to allow custom logic to intervene prior to any generic decision
			if(this.typeAnalyzer.isUnpersistable(type))
			{
				return this.typeHandlerCreator.createTypeHandlerUnpersistable(type);
			}
			
			// there can be enums marked as abstract (yes, they can), so this must come before the abstract check.
			if(XReflect.isEnum(type)) // Class#isEnum is bugged!
			{
				return this.typeHandlerCreator.createTypeHandlerEnum(type);
			}

			// by default same as unpersistable
			if(XReflect.isAbstract(type))
			{
				return this.typeHandlerCreator.createTypeHandlerAbstract(type);
			}
			
			if(Entity.class.isAssignableFrom(type))
			{
				return this.typeHandlerCreator.createTypeHandlerEntity(type);
			}
			
			// check for types to be handled in an abstract way, e.g. java.nio.file.Path
			final PersistenceTypeHandler<D, ? super T> abstractHandler = this.lookupAbstractTypeHandler(type);
			if(abstractHandler != null)
			{
				return abstractHandler;
			}

			// create generic handler for all other cases ("normal" classes without predefined handler)
			return this.typeHandlerCreator.createTypeHandlerGeneric(type);
		}
		
		protected <T> PersistenceTypeHandler<D, ? super T> lookupAbstractTypeHandler(final Class<T> type)
		{
			return this.abstractTypeHandlerSearcher.searchAbstractTypeHandler(type, this.customTypeHandlerRegistry);
		}
		
		protected <T> PersistenceTypeHandler<D, ? super T> searchProvidedTypeHandler(final Class<T> type)
		{
			try
			{
				return Persistence.searchProvidedTypeHandler(this.dataType(), type, null);
			}
			catch(final ReflectiveOperationException e)
			{
				throw new PersistenceException(e);
			}
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
