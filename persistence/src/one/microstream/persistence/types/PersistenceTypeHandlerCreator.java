package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;
import one.microstream.typing.LambdaTypeRecognizer;

public interface PersistenceTypeHandlerCreator<M>
{
	public <T> PersistenceTypeHandler<M, T> createTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	
	
	public abstract class Abstract<M> implements PersistenceTypeHandlerCreator<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeAnalyzer               typeAnalyzer              ;
		final PersistenceTypeResolver               typeResolver              ;
		final PersistenceFieldLengthResolver        lengthResolver            ;
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator;
		final LambdaTypeRecognizer                  lambdaTypeRecognizer      ;
		

		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final PersistenceTypeAnalyzer               typeAnalyzer              ,
			final PersistenceTypeResolver               typeResolver              ,
			final PersistenceFieldLengthResolver        lengthResolver            ,
			final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
			final LambdaTypeRecognizer                  lambdaTypeRecognizer
		)
		{
			super();
			this.typeAnalyzer               = notNull(typeAnalyzer)              ;
			this.typeResolver               = notNull(typeResolver)              ;
			this.lengthResolver             = notNull(lengthResolver)            ;
			this.eagerStoringFieldEvaluator = notNull(eagerStoringFieldEvaluator);
			this.lambdaTypeRecognizer       = notNull(lambdaTypeRecognizer)      ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		public String deriveTypeName(final Class<?> type)
		{
			return this.typeResolver.deriveTypeName(type);
		}
		
		public PersistenceFieldLengthResolver lengthResolver()
		{
			return this.lengthResolver;
		}
		
		public PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator()
		{
			return this.eagerStoringFieldEvaluator;
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> createTypeHandler(final Class<T> type)
		{
			// should never happen or more precisely: should only happen for unhandled primitive types
			if(type.isPrimitive())
			{
				// (29.04.2017 TM)EXCP: proper exception
				throw new RuntimeException(
					"Primitive types must be handled by default (dummy) handler implementations."
				);
			}
			
			// class meta data instances special handling
			if(type == Class.class)
			{
				// (18.09.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Persisting Class instances requires a special-tailored "
					+ PersistenceTypeHandler.class.getSimpleName()
					+ " and cannot be done in a generic way."
				);
			}
			
			// Do NOT replace this with Proxy#isProxyClass. See rationale inside the XReflect method.
			if(XReflect.isProxyClass(type))
			{
				// (20.08.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Proxy classes (subclasses of " + Proxy.class.getName() + ") are not supported."
				);
			}
			
			// array special casing
			if(type.isArray())
			{
				// array special cases
				if(type.getComponentType().isPrimitive())
				{
					// (01.04.2013)EXCP: proper exception
					throw new RuntimeException(
						"Persisting primitive component type arrays requires a special-tailored "
						+ PersistenceTypeHandler.class.getSimpleName()
						+ " and cannot be done in a generic way."
					);
				}
				
				// array types can never change and therefore can never have obsolete types.
				return this.createTypeHandlerArray(type);
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
				// (17.04.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Lambdas are not supported as they cannot be resolved during loading"
					+ " due to insufficient reflection mechanisms provided by Java."
				);
			}
			
			
			// checked first to allow custom logic to intervene prior to any generic decision
			if(this.typeAnalyzer.isUnpersistable(type))
			{
				return this.createTypeHandlerUnpersistable(type);
			}

			// by default same as unpersistable
			if(XReflect.isAbstract(type))
			{
				return this.createTypeHandlerAbstractType(type);
			}
			
			// collections need special handling to avoid dramatically inefficient generic structures
			if(XReflect.isJavaUtilCollectionType(type))
			{
				return this.deriveTypeHandlerCollection(type);
			}
			
			// and another special case
			if(XReflect.isEnum(type)) // Class#isEnum is bugged!
			{
				return this.deriveTypeHandlerEnum(type);
			}

			// create generic handler for all other cases ("normal" classes without predefined handler)
			return this.deriveTypeHandlerEntity(type);
		}
		
		private static void checkNoProblematicFields(final Class<?> type, final XGettingEnum<Field> problematicFields)
		{
			if(problematicFields.isEmpty())
			{
				return;
			}
			
			// (23.07.2019 TM)EXCP: proper exception
			throw new RuntimeException(
				"Type \"" + type.getName() +
				"\" not persistable due to problematic fields "
				+ problematicFields.toString()
			);
		}
		
		protected <T> PersistenceTypeHandler<M, T> deriveTypeHandlerEntity(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEntity(type, persistableFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.createTypeHandlerGeneric(type, persistableFields);
		}
		
		protected <T> PersistenceTypeHandler<M, T> deriveTypeHandlerEnum(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEnum(type, persistableFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.createTypeHandlerEnum(type, persistableFields);
		}
		
		protected <T> PersistenceTypeHandler<M, T> deriveTypeHandlerCollection(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsCollection(type, persistableFields, problematicFields);
			
			if(!problematicFields.isEmpty())
			{
				this.createTypeHandlerGenericCollection(type);
			}

			return this.createTypeHandlerGeneric(type, persistableFields);
		}
		

		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerAbstractType(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerUnpersistable(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerEnum(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerArray(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerGeneric(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerGenericStateless(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerGenericCollection(
			Class<T> type
		);
		
	}
	
}
