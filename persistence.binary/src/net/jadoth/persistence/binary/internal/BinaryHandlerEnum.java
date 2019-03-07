package net.jadoth.persistence.binary.internal;

import java.lang.reflect.Field;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceLoadHandler;

public final class BinaryHandlerEnum<T extends Enum<T>> extends AbstractBinaryHandlerReflective<T>
{
	public static <T extends Enum<T>> BinaryHandlerEnum<T> New(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerEnum<>(
			type                      ,
			allFields                 ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			switchByteOrder
		);
	}
	
	/* (07.11.2013 TM)TODO: enum BinaryHandler special case implementation
	 * (09.06.2017 TM)NOTE:
	 * This is more complex than it appeared at first.
	 * ordinal and name have to be validated instead of being overwritten.
	 * Type validation would actually be required to make that check at startup.
	 * Type refactoring would have to cover changed enums, as well.
	 * Both items mean that type checking all of a sudden has to reach into data values of persisted instances.
	 * Gigantic complication just to cover a syntax sugar language feature with at best questionable use and
	 * viability.
	 * 
	 * Currently, it is seen as the most reasonable strategy to not support language enums at all in entity
	 * persistence. A user how desires to use them can very easily workaround it by storing an identifier value
	 * instead (e.g. a simple integer) and resolving that to the desired enum instance and keeping it in a
	 * transient field. Perfect functionality, tiny effort.
	 * As compared to gigantic effort for tiny functionality gain for a generic handlig solution.
	 */
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static <E extends Enum<E>> EqHashTable<String, E> initializeEnumReferencesCache(final Class<E> type)
	{
		final EqHashTable<String, E> cachedEnumReferences = EqHashTable.New();
		
		final E[] enumConstants = type.getEnumConstants();
		for(final E e : enumConstants)
		{
			cachedEnumReferences.add(e.name(), e);
		}
		
		return cachedEnumReferences;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * Cached, because the weird tinkering in the JDK code is creepy.
	 */
	private final EqHashTable<String, T> cachedEnumReferences;
	
	// (09.06.2017 TM)NOTE: would have to hold fields to orinal and name here as special cases
	



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected BinaryHandlerEnum(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type, allFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		this.cachedEnumReferences = initializeEnumReferencesCache(type);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private String getEnumName(final Binary bytes)
	{
		/* (09.06.2017 TM)FIXME: BinaryHandlerEnum#getEnumName()
		 * Must use bytes.buildItemAddress() plus offset to the Enum#name field
		 * Hm. But that is a String reference and not present, yet.
		 * Maybe use the ordinal after all? In the end, that has to be consistent, anyway.
		 */
//		return this.instantiator.newInstance();
		
		throw new net.jadoth.meta.NotImplementedYetError();
	}
	
	@Override
	public final T create(final Binary bytes)
	{
		// enum constant do not get created, but only looked up instead.
		
		final String enumConstantName = this.getEnumName(bytes);
		final T enumConstant = this.cachedEnumReferences.get(enumConstantName);
		return enumConstant;
	}
	
	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler builder)
	{
		/* (09.06.2017 TM)FIXME: BinaryHandlerEnum#update()
		 * must not set Enum#ordinal and Enum#name, but rather validate the loaded data's consistency in regard
		 * to them.
		 * Only the other fields may get updated.
		 * 
		 */
		throw new net.jadoth.meta.NotImplementedYetError();
	}

}
