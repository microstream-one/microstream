package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.types.XAddingEnum;
import one.microstream.collections.types.XAddingSequence;
import one.microstream.collections.types.XPrependingEnum;
import one.microstream.collections.types.XPrependingSequence;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeAnalyzer
{
	public boolean isUnpersistable(Class<?> type);
	
	public <C extends XPrependingEnum<Field>> C collectPersistableFieldsEntity(
		Class<?>           type             ,
		C                  persistableFields,
		XAddingEnum<Field> problematicFields
	);
	
	public <C extends XPrependingEnum<Field>> C collectPersistableFieldsCollection(
		Class<?>           type             ,
		C                  persistableFields,
		XAddingEnum<Field> problematicFields
	);
	
	public <C extends XPrependingEnum<Field>> C collectPersistableFieldsEnum(
		Class<?>           type             ,
		C                  persistableFields,
		XAddingEnum<Field> problematicFields
	);


	
	public static PersistenceTypeAnalyzer New(
		final PersistenceTypeEvaluator  isPersistable                    ,
		final PersistenceFieldEvaluator fieldSelectorPersistable         ,
		final PersistenceFieldEvaluator fieldSelectorEnum                ,
		final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
	)
	{
		return new PersistenceTypeAnalyzer.Default(
			notNull(isPersistable),
			notNull(fieldSelectorPersistable),
			notNull(fieldSelectorEnum),
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
			final XPrependingSequence<Field> persistableFields,
			final PersistenceFieldEvaluator  isProblematic    ,
			final XAddingSequence<Field>     problematicFields
		)
		{
			XReflect.iterateDeclaredFieldsUpwards(entityType, field ->
			{
				// non-instance fields are always discarded
				if(!XReflect.isInstanceField(field))
				{
					return;
				}
				
				// non-persistable fields are discard
				if(!isPersistable.applies(entityType, field))
				{
					return;
				}
				
				/*
				 * if there is a "problematic" filter and it applies, the field is registered as such
				 * Note: there is a difference between being not persistable and being problematic.
				 * Not persistable fields are simply ignored, e.g. transient fields.
				 * Problematic fields cannot be ignored but require special behavior as a consequence,
				 * usually an exception about not being generically analyzable.
				 */
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
		private final PersistenceFieldEvaluator fieldSelectorEnum;
		private final PersistenceFieldEvaluator fieldSelectorReflectiveCollection;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeEvaluator  isPersistable                    ,
			final PersistenceFieldEvaluator fieldSelectorPersistable         ,
			final PersistenceFieldEvaluator fieldSelectorEnum                ,
			final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
		)
		{
			super();
			this.isPersistable                     = isPersistable                    ;
			this.fieldSelectorPersistable          = fieldSelectorPersistable         ;
			this.fieldSelectorEnum                 = fieldSelectorEnum                ;
			this.fieldSelectorReflectiveCollection = fieldSelectorReflectiveCollection;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean isUnpersistable(final Class<?> type)
		{
			return !this.isPersistable.isPersistableType(type);
		}

		@Override
		public <C extends XPrependingEnum<Field>> C collectPersistableFieldsEntity(
			final Class<?>           type             ,
			final C                  persistableFields,
			final XAddingEnum<Field> problematicFields
		)
		{
			iterateInstanceFields(
				type,
				this.fieldSelectorPersistable,
				persistableFields,
				null,
				problematicFields
			);

			return persistableFields;
		}
		
		@Override
		public <C extends XPrependingEnum<Field>> C collectPersistableFieldsEnum(
			final Class<?>           type             ,
			final C                  persistableFields,
			final XAddingEnum<Field> problematicFields
		)
		{
			iterateInstanceFields(
				type,
				this.fieldSelectorPersistable,
				persistableFields,
				(t, f) ->
					!this.fieldSelectorEnum.applies(type, f),
				problematicFields
			);
						
			return persistableFields;
		}
		
	

		@Override
		public <C extends XPrependingEnum<Field>> C collectPersistableFieldsCollection(
			final Class<?>           type             ,
			final C                  persistableFields,
			final XAddingEnum<Field> problematicFields
		)
		{
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
