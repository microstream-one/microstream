package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.types.XPrependingEnum;
import one.microstream.collections.types.XPrependingSequence;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeAnalyzer
{
	public <C extends XPrependingEnum<Field>> C collectPersistableEntityFields(
		Class<?>               type               ,
		C                      persistableFields  ,
		XPrependingEnum<Field> unpersistableFields
	);
	
	public <C extends XPrependingEnum<Field>> C collectPersistableCollectionFields(
		Class<?>               type               ,
		C                      persistableFields  ,
		XPrependingEnum<Field> unpersistableFields
	);


	
	public static PersistenceTypeAnalyzer New(
		final PersistenceTypeEvaluator  isPersistable                    ,
		final PersistenceFieldEvaluator fieldSelectorReflectiveEntity    ,
		final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
	)
	{
		return new PersistenceTypeAnalyzer.Default(
			notNull(isPersistable),
			notNull(fieldSelectorReflectiveEntity),
			notNull(fieldSelectorReflectiveCollection)
		);
	}

	public final class Default implements PersistenceTypeAnalyzer
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final void iterateInstanceFields(
			final Class<?>                   entityType                 ,
			final XPrependingSequence<Field> persistableFieldCollector  ,
			final XPrependingSequence<Field> unpersistableFieldCollector,
			final PersistenceFieldEvaluator  isPersistable
		)
		{
			XReflect.iterateDeclaredFieldsUpwards(entityType, field ->
			{
				if(!XReflect.isInstanceField(field))
				{
					return;
				}
				
				if(isPersistable.isPersistable(entityType, field))
				{
					persistableFieldCollector.prepend(field);
				}
				else if(unpersistableFieldCollector != null)
				{
					unpersistableFieldCollector.prepend(field);
				}
			});
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeEvaluator  isPersistable;
		private final PersistenceFieldEvaluator fieldSelectorReflectiveEntity    ;
		private final PersistenceFieldEvaluator fieldSelectorReflectiveCollection;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeEvaluator  isPersistable                    ,
			final PersistenceFieldEvaluator fieldSelectorReflectiveEntity    ,
			final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
		)
		{
			super();
			this.isPersistable                     = isPersistable;
			this.fieldSelectorReflectiveEntity     = fieldSelectorReflectiveEntity;
			this.fieldSelectorReflectiveCollection = fieldSelectorReflectiveCollection;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <C extends XPrependingEnum<Field>> C collectPersistableEntityFields(
			final Class<?>               type               ,
			final C                      persistableFields  ,
			final XPrependingEnum<Field> unpersistableFields
		)
		{
			if(!this.isNonAbstractTypeValidating(type))
			{
				// handle abstract types as having no fields at all / stateless types.
				return persistableFields;
			}

			iterateInstanceFields(type, persistableFields, unpersistableFields, this.fieldSelectorReflectiveEntity);

			return persistableFields;
		}
		
		private boolean isNonAbstractTypeValidating(final Class<?> type)
		{
			/*
			 * tricky:
			 * all abstract types (both interfaces and classes) can be handled as having no fields at all
			 * because there can never be actual instances of exactely (only) that type encountered
			 * that would have to be persistet.
			 * However, a type entry for those abstract classes is still necessary for typeId validation purposes.
			 * 
			 * Checking for abstract types comes even before checking for persistability intentionally as
			 * a persistence layer only has to handle concrete types anyway.
			 * This means for example a type definition string is exported for them, but it will be empty.
			 * This is meant by design and not an error. If it turns out to cause problems, it has to be fixed
			 * and commented in here accordingly.
			 */
			if(XReflect.isAbstract(type))
			{
				return false;
			}
			
			if(!this.isPersistable.isPersistableType(type))
			{
				throw new PersistenceExceptionTypeNotPersistable(type);
			}
			
			// non-abstract and persistable type
			return true;
		}

		@Override
		public <C extends XPrependingEnum<Field>> C collectPersistableCollectionFields(
			final Class<?>               type               ,
			final C                      persistableFields  ,
			final XPrependingEnum<Field> unpersistableFields
		)
		{
			if(!this.isNonAbstractTypeValidating(type))
			{
				// handle abstract types as having no fields at all / stateless types.
				return persistableFields;
			}

			iterateInstanceFields(type, persistableFields, unpersistableFields, this.fieldSelectorReflectiveCollection);
			
			return persistableFields;
		}

	}

}
