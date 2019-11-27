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
import one.microstream.java.lang.BinaryHandlerNativeArrayObject;
import one.microstream.java.util.BinaryHandlerGenericCollection;
import one.microstream.java.util.BinaryHandlerGenericList;
import one.microstream.java.util.BinaryHandlerGenericMap;
import one.microstream.java.util.BinaryHandlerGenericQueue;
import one.microstream.java.util.BinaryHandlerGenericSet;
import one.microstream.persistence.binary.internal.BinaryHandlerAbstractType;
import one.microstream.persistence.binary.internal.BinaryHandlerGenericEnum;
import one.microstream.persistence.binary.internal.BinaryHandlerGenericType;
import one.microstream.persistence.binary.internal.BinaryHandlerStateless;
import one.microstream.persistence.binary.internal.BinaryHandlerUnpersistable;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceTypeAnalyzer;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceTypeInstantiatorProvider;
import one.microstream.persistence.types.PersistenceTypeResolver;
import one.microstream.typing.LambdaTypeRecognizer;


public interface BinaryTypeHandlerCreator extends PersistenceTypeHandlerCreator<Binary>
{
	@Override
	public <T> PersistenceTypeHandler<Binary, T> createTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;


	
	public static BinaryTypeHandlerCreator New(
		final PersistenceTypeAnalyzer                     typeAnalyzer              ,
		final PersistenceTypeResolver                     typeResolver              ,
		final PersistenceFieldLengthResolver              lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator       eagerStoringFieldEvaluator,
		final LambdaTypeRecognizer                        lambdaTypeRecognizer      ,
		final PersistenceTypeInstantiatorProvider<Binary> instantiatorProvider      ,
		final boolean                                     switchByteOrder
	)
	{
		return new BinaryTypeHandlerCreator.Default(
			notNull(typeAnalyzer)              ,
			notNull(typeResolver)              ,
			notNull(lengthResolver)            ,
			notNull(eagerStoringFieldEvaluator),
			notNull(lambdaTypeRecognizer)      ,
			notNull(instantiatorProvider)      ,
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
		
		final PersistenceTypeInstantiatorProvider<Binary> instantiatorProvider;
		final boolean                                     switchByteOrder     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeAnalyzer                     typeAnalyzer              ,
			final PersistenceTypeResolver                     typeResolver              ,
			final PersistenceFieldLengthResolver              lengthResolver            ,
			final PersistenceEagerStoringFieldEvaluator       eagerStoringFieldEvaluator,
			final LambdaTypeRecognizer                        lambdaTypeRecognizer      ,
			final PersistenceTypeInstantiatorProvider<Binary> instantiatorProvider      ,
			final boolean                                     switchByteOrder
		)
		{
			super(typeAnalyzer, typeResolver, lengthResolver, eagerStoringFieldEvaluator, lambdaTypeRecognizer);
			this.instantiatorProvider = instantiatorProvider;
			this.switchByteOrder      = switchByteOrder     ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerAbstractType(final Class<T> type)
		{
			// type gets a type id assigned and an empty type description, but its instances cannot be persisted.
			return BinaryHandlerAbstractType.New(type);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerUnpersistable(
			final Class<T> type
		)
		{
			// type gets a type id assigned and an empty type description, but its instances cannot be persisted.
			return BinaryHandlerUnpersistable.New(type);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerArray(
			final Class<T> type
		)
		{
			// array types can never change and therefore can never have obsolete types.
			return BinaryHandlerNativeArrayObject.New(type);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerGenericStateless(
			final Class<T> type
		)
		{
			return BinaryHandlerStateless.New(type);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerGeneric(
			final Class<T>            type             ,
			final XGettingEnum<Field> persistableFields
		)
		{
			/* (16.07.2019 TM)TODO: priv#122 ensure type handler for persistable field type?
			 * This is not done yet. For example: Analysing JDK collections that have a field of type
			 * java.util.Comparator don't cause the Comparator itself to be analyzed about its persistability.
			 * 
			 * Note: it actually is already done, see PersistenceTypeHandlerManager$Default#internalRegisterTypeHandler.
			 * But that seems to be a rather naive approch, ignoring all considerations about interlinked
			 * persistability problems below.
			 * 
			 * A recursive mechanism would be is required to ensure that.
			 * However, this would be rather complex, since there might be looping type references:
			 * A has a field of type B, but B has a field of type A.
			 * So a kind of "pending types" XEnum<Class<?>> would have to be passed along to prevent a stack overflow.
			 * Also, the type handling ensuring logic called here would have to register a created TypeHandler
			 * for it to be lookup'able when the type is encountered the next time.
			 * 
			 * But wait: isn't it even more complex?
			 * Say class A has two fields of types class B and type C.
			 * B has a field of Type A.
			 * 
			 * When analyzing A, type B would have to be analyzed accordingly.
			 * A is a "pending" type assumed to be persistable, so B gets a type handler created.
			 * Without such an assumption, nothing no type handler could be created, since B would wait for A
			 * and A would wait for B.
			 * The analyzing process comes back to A and now needs to analyze type C.
			 * However, C is unpersistable (e.g. references a Thread)
			 * So A is not persistable, either.
			 * And that in turn means, that B is actually not persistable, despite its TypeHandler having been
			 * created already.
			 * 
			 * The solution would be:
			 * TypeHandlers created by the recursive analysis here must be registered in a preliminary lookup (simple map).
			 * Only after all reachable types are determined to be properly persistable, all the preliminary
			 * Type handlers may be seen as "valid" and be registered.
			 * 
			 * OR the assumption above would have to be replaced by a two-phased type analysis:
			 * First analyze all pending types ignoring the pending types themselves.
			 * If no more new pending type is encountered and there was no error regarding persistability of any of the
			 * pending types, then all pending types can have type handlers created and registered right away.
			 * However, this seems to be much more complicated than the approach with the temporary type handlers
			 * and later registration
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
			
			if(persistableFields.isEmpty())
			{
				return this.createTypeHandlerGenericStateless(type);
			}

			// default implementation simply always uses a blank memory instantiator
			return BinaryHandlerGenericType.New(
				type,
				this.deriveTypeName(type),
				persistableFields,
				this.lengthResolver(),
				this.eagerStoringFieldEvaluator(),
				this.instantiatorProvider.provideTypeInstantiator(type),
				this.switchByteOrder
			);
		}

		// all casts are type checked dynamically, but the compiler doesn't understand that
//		@SuppressWarnings("unchecked")
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> deriveTypeHandlerGenericPath(
			final Class<T> type
		)
		{
			/* (27.11.2019 TM)FIXME: BinaryHandlerPath
			 * See comment there.
			 */
			throw new one.microstream.meta.NotImplementedYetError();
			
//			return (PersistenceTypeHandler<Binary, T>)BinaryHandlerPath.New();
		}
		
		// all casts are type checked dynamically, but the compiler doesn't understand that
		@SuppressWarnings("unchecked")
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerGenericJavaUtilCollection(
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
				
				// Fallback-fallback for collections that are neither a Queue, List, Set or Map.
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
				"Collection type cannot be handled generically and requires a custom "
				+ PersistenceTypeHandler.class.getName() + " to be registered: "
				+ type,
				cause
			);
		}
		
		@Override
		protected <T> PersistenceTypeHandler<Binary, T> createTypeHandlerEnum(
			final Class<T>            type             ,
			final XGettingEnum<Field> persistableFields
		)
		{
			return this.createEnumHandler(type, persistableFields);
		}

		@SuppressWarnings("unchecked") // required generics crazy sh*t tinkering
		final <T, E extends Enum<E>> PersistenceTypeHandler<Binary, T> createEnumHandler(
			final Class<?>            type             ,
			final XGettingEnum<Field> persistableFields
		)
		{
			return (PersistenceTypeHandler<Binary, T>)BinaryHandlerGenericEnum.New(
				(Class<E>)type                   ,
				this.deriveTypeName(type)        ,
				persistableFields                ,
				this.lengthResolver()            ,
				this.eagerStoringFieldEvaluator(),
				this.switchByteOrder
			);
		}

	}

}
