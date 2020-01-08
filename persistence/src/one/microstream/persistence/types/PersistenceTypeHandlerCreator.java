package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Path;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;
import one.microstream.typing.LambdaTypeRecognizer;

public interface PersistenceTypeHandlerCreator<D>
{
	public <T> PersistenceTypeHandler<D, T> createTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	
	
	public abstract class Abstract<D> implements PersistenceTypeHandlerCreator<D>
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
		public <T> PersistenceTypeHandler<D, T> createTypeHandler(final Class<T> type)
		{
			// should never happen or more precisely: should only happen for unhandled primitive types
			if(type.isPrimitive())
			{
				// (29.04.2017 TM)EXCP: proper exception
				throw new PersistenceException(
					"Primitive types must be handled by default (dummy) handler implementations."
				);
			}
			
			// class meta data instances special handling
			if(type == Class.class)
			{
				// (18.09.2018 TM)EXCP: proper exception
				throw new PersistenceException(
					"Persisting Class instances requires a special-tailored "
					+ PersistenceTypeHandler.class.getSimpleName()
					+ " and cannot be done in a generic way."
				);
			}
			
			// Do NOT replace this with Proxy#isProxyClass. See rationale inside the XReflect method.
			if(XReflect.isProxyClass(type))
			{
				// (20.08.2019 TM)EXCP: proper exception
				throw new PersistenceException(
					"Proxy classes (subclasses of " + Proxy.class.getName() + ") are not supported."
				);
			}
			
			// array special casing
			if(type.isArray())
			{
				// array special cases
				if(type.getComponentType().isPrimitive())
				{
					// (01.04.2013 TM)EXCP: proper exception
					throw new PersistenceException(
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
				throw new PersistenceException(
					"Lambdas are not supported as they cannot be resolved during loading"
					+ " due to insufficient reflection mechanisms provided by Java."
				);
			}
			
			
			// checked first to allow custom logic to intervene prior to any generic decision
			if(this.typeAnalyzer.isUnpersistable(type))
			{
				return this.createTypeHandlerUnpersistable(type);
			}
			
			// there can be enums marked as abstract (yes, they can), so this must come before the abstract check.
			if(XReflect.isEnum(type)) // Class#isEnum is bugged!
			{
				return this.deriveTypeHandlerEnum(type);
			}

			// by default same as unpersistable
			if(XReflect.isAbstract(type))
			{
				return this.createTypeHandlerAbstractType(type);
			}
			
			/* (27.11.2019 TM)TODO: priv#186: interface type handling abstraction
			 * Hardcoding every interface that needs special treatment here is not a good solution.
			 * Instead, a "interface -> SpecialTypeCreator" registry has to be implemented here,
			 * with the current two cases as default entries and potentially more to come.
			 * Including customized entries, of course.
			 * 
			 * Actually, this could and would have to include abstract classes as well.
			 */
			
			// another special handling for the Path interface, but this is not a good solution. See the TODO.
			if(Path.class.isAssignableFrom(type))
			{
				return this.deriveTypeHandlerGenericPath(type);
			}
			
			// collections need special handling to avoid dramatically inefficient generic structures
			if(XReflect.isJavaUtilCollectionType(type))
			{
				return this.deriveTypeHandlerJavaUtilCollection(type);
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
			throw new PersistenceException(
				"Type \"" + type.getName() +
				"\" not persistable due to problematic fields "
				+ problematicFields.toString()
			);
		}
		
		protected <T> PersistenceTypeHandler<D, T> deriveTypeHandlerEntity(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEntity(type, persistableFields, persisterFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.createTypeHandlerGeneric(type, persistableFields, persisterFields);
		}
		
		protected <T> PersistenceTypeHandler<D, T> deriveTypeHandlerEnum(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEnum(type, persistableFields, persisterFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.createTypeHandlerEnum(type, persistableFields, persisterFields);
		}
		
		protected abstract <T> PersistenceTypeHandler<D, T> deriveTypeHandlerGenericPath(Class<T> type);
		
		protected <T> PersistenceTypeHandler<D, T> deriveTypeHandlerJavaUtilCollection(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsCollection(type, persistableFields, persisterFields, problematicFields);
			
			if(!problematicFields.isEmpty())
			{
				this.createTypeHandlerGenericJavaUtilCollection(type);
			}

			return this.createTypeHandlerGeneric(type, persistableFields, persisterFields);
		}

		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerAbstractType(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerUnpersistable(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerEnum(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields,
			XGettingEnum<Field> persisterFields
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerArray(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerGeneric(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields,
			XGettingEnum<Field> persisterFields
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerGenericStateless(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> createTypeHandlerGenericJavaUtilCollection(
			Class<T> type
		);
		
	}
	
}
