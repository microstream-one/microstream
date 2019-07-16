package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.types.XAddingEnum;
import one.microstream.collections.types.XAddingSequence;
import one.microstream.collections.types.XPrependingEnum;
import one.microstream.collections.types.XPrependingSequence;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeAnalyzer
{
	public <C extends XPrependingEnum<Field>> C collectPersistableEntityFields(
		Class<?> type             ,
		C        persistableFields
	);
	
	public <C extends XPrependingEnum<Field>> C collectPersistableCollectionFields(
		Class<?>           type             ,
		C                  persistableFields,
		XAddingEnum<Field> problematicFields
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
			final Class<?>                   entityType       ,
			final PersistenceFieldEvaluator  isPersistable    ,
			final XPrependingSequence<Field> persistableFields
		)
		{
			iterateInstanceFields(entityType, isPersistable, persistableFields, null, null);
		}

		public static final void iterateInstanceFields(
			final Class<?>                   entityType       ,
			final PersistenceFieldEvaluator  isPersistable    ,
			final XPrependingSequence<Field> persistableFields,
			final PersistenceFieldEvaluator  isProblematic    ,
			final XAddingSequence<Field>     problematicFields
		)
		{
			XReflect.iterateDeclaredFieldsUpwards(entityType, field ->
			{
				// non-instance fielsd are always discarded
				if(!XReflect.isInstanceField(field))
				{
					return;
				}
				
				// non-persistable fields are discard
				if(!isPersistable.applies(entityType, field))
				{
					return;
				}
				
				// if there is a "problematic" filter and it applies, the field is registered as such
				if(isProblematic != null && isProblematic.applies(entityType, field))
				{
					problematicFields.add(field);
					return;
				}
				
				// persistable, non-problematic instance-field
				persistableFields.prepend(field);
			});
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeEvaluator  isPersistable;
		private final PersistenceFieldEvaluator fieldSelectorPersistable;
		private final PersistenceFieldEvaluator fieldSelectorReflectiveCollection;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeEvaluator  isPersistable                    ,
			final PersistenceFieldEvaluator fieldSelectorPersistable         ,
			final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
		)
		{
			super();
			this.isPersistable                     = isPersistable;
			this.fieldSelectorPersistable          = fieldSelectorPersistable;
			this.fieldSelectorReflectiveCollection = fieldSelectorReflectiveCollection;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <C extends XPrependingEnum<Field>> C collectPersistableEntityFields(
			final Class<?> type             ,
			final C        persistableFields
		)
		{
			if(!this.isNonAbstractTypeValidating(type))
			{
				// handle abstract types as having no fields at all / stateless types.
				return persistableFields;
			}

			iterateInstanceFields(type, this.fieldSelectorPersistable, persistableFields);

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
			final Class<?>           type             ,
			final C                  persistableFields,
			final XAddingEnum<Field> problematicFields
		)
		{
			if(!this.isNonAbstractTypeValidating(type))
			{
				// handle abstract types as having no fields at all / stateless types.
				return persistableFields;
			}

			iterateInstanceFields(
				type,
				this.fieldSelectorPersistable,
				persistableFields,
				(t, f) ->
					!this.fieldSelectorReflectiveCollection.applies(type, f),
				problematicFields
			);
						
			return persistableFields;
		}

	}

}
