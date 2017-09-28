package net.jadoth.persistence.binary.types;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArrayObject;
import net.jadoth.persistence.binary.internal.BinaryHandlerStateless;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceTypeAnalyzer;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurer;


/**
 * Called "ensurer", because depending on the case, if creates new type handler or it just initializes
 * already existing, pre-registered ones. So "ensuring" is the most suited common denominator.
 * 
 * @author TM
 */
public interface BinaryTypeHandlerEnsurer extends PersistenceTypeHandlerEnsurer<Binary>
{
	@Override
	public <T> PersistenceTypeHandler<Binary, T> ensureTypeHandler(
		Class<T>           type
//		long               typeId     ,
//		SwizzleTypeManager typeManager
	) throws PersistenceExceptionTypeNotPersistable;


	
	public static BinaryTypeHandlerEnsurer.Implementation New(
		final PersistenceCustomTypeHandlerRegistry<Binary> customTypeHandlerRegistry,
		final PersistenceTypeAnalyzer                      typeAnalyzer             ,
		final PersistenceFieldLengthResolver               lengthResolver
	)
	{
		return new BinaryTypeHandlerEnsurer.Implementation(
			notNull(customTypeHandlerRegistry),
			notNull(typeAnalyzer)             ,
			notNull(lengthResolver)
		);
	}

	public class Implementation implements BinaryTypeHandlerEnsurer
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final PersistenceCustomTypeHandlerRegistry<Binary> customTypeHandlerRegistry;
		final PersistenceTypeAnalyzer                      typeAnalyzer             ;
		final PersistenceFieldLengthResolver               lengthResolver           ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceCustomTypeHandlerRegistry<Binary> customTypeHandlerRegistry,
			final PersistenceTypeAnalyzer                      typeAnalyzer             ,
			final PersistenceFieldLengthResolver               lengthResolver
		)
		{
			super();
			this.customTypeHandlerRegistry = customTypeHandlerRegistry;
			this.typeAnalyzer              = typeAnalyzer             ;
			this.lengthResolver            = lengthResolver           ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public <T> PersistenceTypeHandler<Binary, T> ensureTypeHandler(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			// lookup predefined handler first to cover primitives and to give custom handlers precedence
			final PersistenceTypeHandler<Binary, T> customHandler = this.customTypeHandlerRegistry.lookupTypeHandler(type);
			if(customHandler != null)
			{
				return customHandler;
			}
			
			// should never happen or more precisely: should only happen for unhandled primitives
			if(type.isPrimitive())
			{
				// (29.04.2017 TM)EXCP: proper exception
				throw new RuntimeException("Primitive type values cannot be handled as instances.");
			}
			
			// array special casing
			if(type.isArray())
			{
				// array special cases
				if(type.getComponentType().isPrimitive())
				{
					// (01.04.2013)EXCP: proper exception
					throw new RuntimeException("Primitive component type arrays must be covered by default handler implementations.");
				}
				
				// array types can never change and therefore can never have obsolete types.
				return new BinaryHandlerNativeArrayObject<>(type)/*.initialize(typeId, X.emptyTable())*/;
			}

			// create generic handler for all other cases ("normal" classes without predefined handler)
			return this.createGenericHandler(type);
		}
		
		final <T> PersistenceTypeHandler<Binary, T> createGenericHandler(final Class<T> type)
		{
			final HashEnum<PersistenceTypeDescriptionMemberField> fieldDescriptions = HashEnum.New();

			final XGettingEnum<Field> persistableFields =
				this.typeAnalyzer.collectPersistableFields(type,/* typeManager,*/ fieldDescriptions)
			;

			if(persistableFields.isEmpty())
			{
				// required for a) sparing unnecessary overhead and b) validation reasons
				return new BinaryHandlerStateless<>(type)/*.initialize(typeId, X.emptyTable())*/;
			}
			
			if(type.isEnum())
			{
				/* (09.06.2017 TM)TODO: enum BinaryHandler special case implementation once completed
				 * (10.06.2017 TM)NOTE: not sure if handling enums (constants) in an entity graph
				 * makes sense in the first place. The whole enum concept (the identity of an instance depending
				 *  on the name and/or the order of the field referencing it) is just too wacky for an entity graph.
				 * Use enums for logic, if you must, but keep them out of proper entity graphs.
				 */
//				return this.createEnumHandler(type, typeId, persistableFields, this.lengthResolver);
			}

			// default implementation simply always uses a blank memory instantiator
			return new BinaryHandlerGeneric<>(
				type                                           ,
//				typeId                                         ,
				0L                                             , // typeId gets initialized later for generic handlers
				BinaryPersistence.blankMemoryInstantiator(type),
				persistableFields                              ,
				this.lengthResolver
			);
		}
		
		@SuppressWarnings("unchecked") // required generics crazy sh*t tinkering
		final <T, E extends Enum<E>> PersistenceTypeHandler<Binary, T> createEnumHandler(
			final Class<?>                       type          ,
			final long                           tid           ,
			final XGettingEnum<Field>            allFields     ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			return (PersistenceTypeHandler<Binary, T>)new BinaryHandlerEnum<>(
				(Class<E>)type     ,
				tid                ,
				allFields          ,
				this.lengthResolver
			);
		}

	}

}
