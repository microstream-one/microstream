package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.java.BinaryHandlerEnum;
import one.microstream.java.lang.BinaryHandlerNativeArrayObject;
import one.microstream.java.util.BinaryHandlerGenericCollection;
import one.microstream.java.util.BinaryHandlerGenericList;
import one.microstream.java.util.BinaryHandlerGenericMap;
import one.microstream.java.util.BinaryHandlerGenericQueue;
import one.microstream.java.util.BinaryHandlerGenericSet;
import one.microstream.persistence.binary.internal.BinaryHandlerGenericType;
import one.microstream.persistence.binary.internal.BinaryHandlerStateless;
import one.microstream.persistence.binary.internal.BinaryHandlerUnpersistable;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceTypeAnalyzer;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerCreator;
import one.microstream.reflect.XReflect;
import one.microstream.typing.LambdaTypeRecognizer;


public interface BinaryTypeHandlerCreator extends PersistenceTypeHandlerCreator<Binary>
{
	@Override
	public <T> PersistenceTypeHandler<Binary, T> createTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;


	
	public static BinaryTypeHandlerCreator New(
		final PersistenceTypeAnalyzer               typeAnalyzer              ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final LambdaTypeRecognizer                  lambdaTypeRecognizer      ,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryTypeHandlerCreator.Default(
			notNull(typeAnalyzer)              ,
			notNull(lengthResolver)            ,
			notNull(eagerStoringFieldEvaluator),
			notNull(lambdaTypeRecognizer)      ,
			switchByteOrder
		);
	}

	public class Default
	extends PersistenceTypeHandlerCreator.Abstract<Binary>
	implements BinaryTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final boolean switchByteOrder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeAnalyzer               typeAnalyzer              ,
			final PersistenceFieldLengthResolver        lengthResolver            ,
			final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
			final LambdaTypeRecognizer                  lambdaTypeRecognizer      ,
			final boolean                               switchByteOrder
		)
		{
			super(typeAnalyzer, lengthResolver, eagerStoringFieldEvaluator, lambdaTypeRecognizer);
			this.switchByteOrder = switchByteOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerArray(final Class<T> type)
		{
			// array types can never change and therefore can never have obsolete types.
			return new BinaryHandlerNativeArrayObject<>(type);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerReflective(
			final Class<T>            type             ,
			final XGettingEnum<Field> persistableFields
		)
		{
			if(type.isEnum())
			{
				/* (09.06.2017 TM)TODO: enum BinaryHandler special case implementation once completed
				 * (10.06.2017 TM)NOTE: not sure if handling enums (constants) in an entity graph
				 * makes sense in the first place. The whole enum concept (the identity of an instance depending
				 * on the name and/or the order of the field referencing it) is just too wacky for an entity graph.
				 * Use enums for logic, if you must, but keep them out of proper entity graphs.
				 */
//				return this.createEnumHandler(type, persistableFields);
				// (12.07.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Handling Java language enums is currently not supported since changes to the enum constants,"
					+ " a part of the type definition, would require changes to data and might even be ambiguous."
					+ " Please consider that enums are merely a syntax sugar helper for building logic,"
					+ " not a suitable construct to be used in a persisted entity graph."
				);
			}
			
			if(persistableFields.isEmpty())
			{
				if(XReflect.isAbstract(type))
				{
					return new BinaryHandlerUnpersistable<>(type);
				}
				
				return new BinaryHandlerStateless<>(type);
			}
			
			if(XReflect.isAbstract(type))
			{
				// (16.07.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Cannot create a non-stateless instances type handler for abstract type "
					+ type
				);
			}
			
			/* (16.07.2019 TM)TODO: ensure type handler for persistable field type?
			 * This is not done yet. For example: Analysing JDK collections that have a field of type
			 * java.util.Comparator don't cause the Comparator itself to be analyzed about its persistability.
			 * 
			 * A recursive mechanism similar to super class in PersistenceTypeHandlerManager#internalEnsureTypeHandler
			 * is required.
			 * However, this would be more complex, since there might be looping type references:
			 * A has a field of type B, but B has a field of type A.
			 * So a kind of "pending types" XEnum<Class<?>> would have to be passed along to prevent a stack overflow
			 * Also, the type handling ensuring logic called here would have to register a created TypeHandler
			 * for it to be lookup'able when the type is encountered the next time.
			 * 
			 * But wait: isn't it even more complex?
			 * Say class A has two fields of types class B and type C.
			 * B has a field of Type A.
			 * 
			 * When analyzing A, type B would have to be analyzed accordingly.
			 * A is a "pending" type assumed to be persistable, so B gets a type handler created.
			 * The analying process comes back to A and now needs to analyse type C.
			 * However, C is unpersistable (e.g. refernces a Thread)
			 * So A is not persistable, either.
			 * And that in turn means, that B is actually not persistable, despite its TypeHandler having been
			 * created already.
			 * 
			 * The solution would be:
			 * TypeHandlers created by the recursive analysis here must be registered in a preliminary lookup (simple map).
			 * Only after all reachable types are determined to be properly persistable, all the preliminary
			 * Type handlers may be registered as being "valid".
			 * 
			 * The change wouldn't have to be that big, actually:
			 * The recursive thing is something internal in here.
			 * It's just that a collection of TypeHandlers must be returned.
			 * 
			 * No, not sufficient: the referenced type may already have a handler and if not, it needs a typeId.
			 * So the TypeHandlerManager has to be passed here as a callback option.
			 * Then maybe the return type can stay a single TypeHandler, because all the recursively created
			 * type handlers can be registered at the passed TypeHandlerManager as a side effect.
			 * 
			 * OR maybe ALL created type handlers should be registered at the passed TypeHandlerManager this way.
			 * 
			 */

			// default implementation simply always uses a blank memory instantiator
			return BinaryHandlerGenericType.New(
				type                                           ,
				persistableFields                              ,
				this.lengthResolver()                          ,
				this.eagerStoringFieldEvaluator()              ,
				BinaryPersistence.blankMemoryInstantiator(type),
				this.switchByteOrder
			);
		}
		
		// all casts are type checked dynamically, but the compiler doesn't understand that
		@SuppressWarnings("unchecked")
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerGenericCollection(
			final Class<T> type
		)
		{
			Throwable cause;
			try
			{
				if(Queue.class.isAssignableFrom(type))
				{
					return (PersistenceTypeHandler<Binary, T>)BinaryHandlerGenericQueue.New((Class<Queue<?>>)type);
				}
				if(List.class.isAssignableFrom(type))
				{
					return (PersistenceTypeHandler<Binary, T>)BinaryHandlerGenericList.New((Class<List<?>>)type);
				}
				if(Set.class.isAssignableFrom(type))
				{
					return (PersistenceTypeHandler<Binary, T>)BinaryHandlerGenericSet.New((Class<Set<?>>)type);
				}
				if(Map.class.isAssignableFrom(type))
				{
					return (PersistenceTypeHandler<Binary, T>)BinaryHandlerGenericMap.New((Class<Map<?, ?>>)type);
				}
				
				/*
				 * Since this method is only entered if type is either a Collection or a Map, this check should
				 * be superfluos. But it's a nice defense against an unforeseen change in the checking method.
				 */
				if(Collection.class.isAssignableFrom(type))
				{
					return (PersistenceTypeHandler<Binary, T>)BinaryHandlerGenericCollection.New((Class<Collection<?>>)type);
				}
				
				// (16.07.2019 TM)EXCP: proper exception
				cause = new RuntimeException("Unhandled collection type: " + type);
			}
			catch(final NoSuchMethodRuntimeException e)
			{
				// fall through to exception
				cause = e;
			}
			
			// this is as far as generic type analysis gets. Surrender.
			
			// (16.07.2019 TM)EXCP: proper exception
			throw new RuntimeException(
				"Collection type cannot be handled generically and required a custom "
				+ PersistenceTypeHandler.class.getName() + " to be registered: "
				+ type,
				cause
			);
		}

		@SuppressWarnings("unchecked") // required generics crazy sh*t tinkering
		final <T, E extends Enum<E>> PersistenceTypeHandler<Binary, T> createEnumHandler(
			final Class<?>            type     ,
			final XGettingEnum<Field> allFields
		)
		{
			return (PersistenceTypeHandler<Binary, T>)BinaryHandlerEnum.New(
				(Class<E>)type                ,
				allFields                     ,
				this.lengthResolver()         ,
				this.eagerStoringFieldEvaluator(),
				this.switchByteOrder
			);
		}

	}

}
