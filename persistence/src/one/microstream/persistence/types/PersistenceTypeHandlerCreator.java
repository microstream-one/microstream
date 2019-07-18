package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

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
		final PersistenceFieldLengthResolver        lengthResolver            ;
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator;
		final LambdaTypeRecognizer                  lambdaTypeRecognizer      ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final PersistenceTypeAnalyzer               typeAnalyzer              ,
			final PersistenceFieldLengthResolver        lengthResolver            ,
			final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
			final LambdaTypeRecognizer                  lambdaTypeRecognizer
		)
		{
			super();
			this.typeAnalyzer               = notNull(typeAnalyzer)              ;
			this.lengthResolver             = notNull(lengthResolver)            ;
			this.eagerStoringFieldEvaluator = notNull(eagerStoringFieldEvaluator);
			this.lambdaTypeRecognizer       = notNull(lambdaTypeRecognizer)      ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
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
			// should never happen or more precisely: should only happen for unhandled primitives
			if(type.isPrimitive())
			{
				// (29.04.2017 TM)EXCP: proper exception
				throw new RuntimeException(
					"Primitive types must be handled by default (dummy) handler implementations."
				);
			}
			
			/*
			 * Since type refactoring, the old and simple strategy to handle Class instances does not work any more.
			 * Class instances are system meta data and should not be stored as user data in a database, anyway.
			 * Register a custom handler if you absolutely must and accept full responsibility for all details and
			 * problems associated with it.
			 */
			if(type == Class.class)
			{
				// (12.07.2019 TM)FIXME: MS-153 gives a solution
				// (18.09.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Class instances are system meta data and should not be stored as user data in a database. "
					+ "Register a custom handler if you absolutely must and accept full responsibility "
					+ "for all details and problems associated with it."
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
						"Primitive component type arrays must be handled by default handler implementations."
					);
				}
				
				// array types can never change and therefore can never have obsolete types.
				return this.createTypeHandlerArray(type);
			}
			
			if(this.lambdaTypeRecognizer.isLambdaType(type))
			{
				// (17.04.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Lambdas are not supported as they cannot be resolved during loading"
					+ " due to insufficient reflection mechanisms provided by Java."
				);
			}
			
			if(XReflect.isJavaUtilCollectionType(type))
			{
				return this.deriveTypeHandlerCollection(type);
			}
			
			/* (25.03.2019 TM)NOTE:
			 * Note on lambdas:
			 * There is (currently) no way of determining if an instance is a lambda.
			 * Any checks on the name are best guesses, not reliable logic.
			 * It may work in certain (even most) applications absolutely correctly, but it is not
			 * absolutely reliable to not be ambiguous and hence wrong.
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
			 * as opposed to inner class types etc., there IS a need to recognize lambdas.)
			 * Or it might not, as long as they maintain their displayed level of competence.
			 * 
			 * Until then:
			 * If required, a a simple solution would be to register a custom LambdaTypeRecognizer
			 * that checks for lambdas with whatever logic works in the particular case.
			 */

			// create generic handler for all other cases ("normal" classes without predefined handler)
			return this.deriveTypeHandlerEntity(type);
		}
		
		protected <T> PersistenceTypeHandler<M, T> deriveTypeHandlerEntity(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableEntityFields(type, persistableFields);

			return this.createTypeHandlerReflective(type, persistableFields);
		}
		
		protected <T> PersistenceTypeHandler<M, T> deriveTypeHandlerCollection(final Class<T> type)
		{
			final HashEnum<Field> persistableFields   = HashEnum.New();
			final HashEnum<Field> unpersistableFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableCollectionFields(type, persistableFields, unpersistableFields);
			
			if(!unpersistableFields.isEmpty())
			{
				this.createTypeHandlerGenericCollection(type);
			}

			return this.createTypeHandlerReflective(type, persistableFields);
		}
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerArray(Class<T> type);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerReflective(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields
		);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerGenericCollection(
			Class<T> type
		);
		
	}
	
}
