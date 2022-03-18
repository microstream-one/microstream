package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import org.slf4j.Logger;

import one.microstream.chars.XChars;
import one.microstream.entity.Entity;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;
import one.microstream.typing.LambdaTypeRecognizer;
import one.microstream.util.logging.Logging;

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
		private final static Logger logger = Logging.getLogger(PersistenceTypeHandlerEnsurer.Default.class);
		
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
				this.logHandlerUsage("provided", providedHandler);
				
				return providedHandler;
			}
			
			// lookup predefined handler first to cover primitives and to give custom handlers precedence
			final PersistenceTypeHandler<D, ? super T> customHandler = this.customTypeHandlerRegistry.lookupTypeHandler(type);
			if(customHandler != null)
			{
				this.logHandlerUsage("predefined", customHandler);
				
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
				final PersistenceTypeHandler<D, T> proxyHandler = this.typeHandlerCreator.createTypeHandlerProxy(type);
				
				this.logHandlerCreation("proxy", proxyHandler);
				
				return proxyHandler;
			}
			
			// array special casing
			if(type.isArray())
			{
				final PersistenceTypeHandler<D, T> arrayHandler = this.typeHandlerCreator.createTypeHandlerArray(type);
				
				this.logHandlerCreation("array", arrayHandler);
				
				return arrayHandler;
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
				final PersistenceTypeHandler<D, T> lambdaHandler = this.typeHandlerCreator.createTypeHandlerLambda(type);
				
				this.logHandlerCreation("lambda", lambdaHandler);
				
				return lambdaHandler;
			}
			
			// checked first to allow custom logic to intervene prior to any generic decision
			if(this.typeAnalyzer.isUnpersistable(type))
			{
				final PersistenceTypeHandler<D, T> handlerUnpersistable = this.typeHandlerCreator.createTypeHandlerUnpersistable(type);
				
				this.logHandlerCreation("non-persisting", handlerUnpersistable);
				
				return handlerUnpersistable;
			}
			
			// there can be enums marked as abstract (yes, they can), so this must come before the abstract check.
			if(XReflect.isEnum(type)) // Class#isEnum is bugged!
			{
				final PersistenceTypeHandler<D, T> enumHandler = this.typeHandlerCreator.createTypeHandlerEnum(type);
				
				this.logHandlerCreation("enum", enumHandler);
				
				return enumHandler;
			}

			// by default same as unpersistable
			if(XReflect.isAbstract(type))
			{
				final PersistenceTypeHandler<D, T> abstractHandler = this.typeHandlerCreator.createTypeHandlerAbstract(type);
				
				this.logHandlerCreation("abstract", abstractHandler);
				
				return abstractHandler;
			}
			
			if(Entity.class.isAssignableFrom(type))
			{
				final PersistenceTypeHandler<D, T> entityHandler = this.typeHandlerCreator.createTypeHandlerEntity(type);
				
				this.logHandlerCreation("entity", entityHandler);
				
				return entityHandler;
			}
			
			// check for types to be handled in an abstract way, e.g. java.nio.file.Path
			final PersistenceTypeHandler<D, ? super T> abstractHandler = this.lookupAbstractTypeHandler(type);
			if(abstractHandler != null)
			{
				this.logHandlerCreation("abstract", abstractHandler);
				
				return abstractHandler;
			}

			// create generic handler for all other cases ("normal" classes without predefined handler)
			final PersistenceTypeHandler<D, T> genericHandler = this.typeHandlerCreator.createTypeHandlerGeneric(type);
					
			this.logHandlerCreation("generic", genericHandler);
			
			return genericHandler;
		}
		
		private void logHandlerUsage(final String handlerType, final PersistenceTypeHandler<?, ?> handler)
		{
			logger.debug(
				"Using {} type handler for {}: {}",
				handlerType,
				handler.type().getName(),
				XChars.systemString(handler)
			);
		}
		
		private void logHandlerCreation(final String handlerType, final PersistenceTypeHandler<?, ?> handler)
		{
			logger.debug(
				"Created {} type handler for {}: {}",
				handlerType,
				handler.type().getName(),
				XChars.systemString(handler)
			);
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
