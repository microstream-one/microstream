package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.java.BinaryHandlerEnum;
import one.microstream.java.lang.BinaryHandlerNativeArrayObject;
import one.microstream.persistence.binary.internal.BinaryHandlerGenericType;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceTypeAnalyzer;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerCreator;
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
