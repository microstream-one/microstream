package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XPrependingEnum;
import one.microstream.collections.types.XPrependingSequence;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeAnalyzer
{
	public XGettingEnum<Field> collectPersistableFieldsReflective(
		Class<?>                                                         type             ,
		XPrependingEnum<PersistenceTypeDescriptionMemberFieldReflective> fieldDescriptions
	);
	
	public XGettingEnum<Field> collectPersistableFieldsReflectiveCollection(
		Class<?>                                                         type             ,
		XPrependingEnum<PersistenceTypeDescriptionMemberFieldReflective> fieldDescriptions
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

		public static final void collectPersistableInstanceFields(
			final XPrependingSequence<Field> persistableFieldCollector,
			final Class<?>                   entityType               ,
			final PersistenceFieldEvaluator  isPersistable
		)
		{
			XReflect.collectTypedFields(persistableFieldCollector, entityType, field ->
			{
				if(!XReflect.isInstanceField(field) || !isPersistable.isPersistable(entityType, field))
				{
					return false;
				}
				
//				typeManager.ensureTypeId(field.getType());
				return true;
			});
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceFieldEvaluator fieldSelectorReflectiveEntity    ;
		private final PersistenceFieldEvaluator fieldSelectorReflectiveCollection;
		private final PersistenceTypeEvaluator  isPersistable;



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
		public XGettingEnum<Field> collectPersistableFieldsReflective(
			final Class<?>                                                         type             ,
			final XPrependingEnum<PersistenceTypeDescriptionMemberFieldReflective> fieldDescriptions
		)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();

			if(!this.isValidTypesNonAbstractType(type))
			{
				// handle abstract types as having no fields at all / stateless types.
				return persistableFields;
			}

			collectPersistableInstanceFields(persistableFields, type,this.fieldSelectorReflectiveEntity);

			return persistableFields;
		}
		
		private boolean isValidTypesNonAbstractType(final Class<?> type)
		{
			if(!this.isPersistable.isPersistableType(type))
			{
				throw new PersistenceExceptionTypeNotPersistable(type);
			}
			
			/*
			 * tricky:
			 * all abstract types (both interfaces and classes) can be handled as having no fields at all
			 * because there can never be actual instances of exactely (only) that type encountered
			 * that would have to be persistet.
			 * However, a type entry for those abstract classes is still necessary for typeId validation purposes.
			 * Checking for abstract types comes even before checking for persistability intentionally as
			 * a persistence layer only has to handle concrete types anyway.
			 * This means for example a type definition string is exported for them, but it will be empty.
			 * This is meant by design and not an error. If it turns out to cause problems, it has to be fixed
			 * and commented in here accordingly.
			 */
			return !XReflect.isAbstract(type);
		}

		@Override
		public XGettingEnum<Field> collectPersistableFieldsReflectiveCollection(
			final Class<?>                                                         type             ,
			final XPrependingEnum<PersistenceTypeDescriptionMemberFieldReflective> fieldDescriptions
		)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();

			if(!this.isValidTypesNonAbstractType(type))
			{
				// handle abstract types as having no fields at all / stateless types.
				return persistableFields;
			}

			// (12.07.2019 TM)FIXME: MS-143: collect unpersistable fields as well and throw exception if not emptys
			collectPersistableInstanceFields(persistableFields, type,this.fieldSelectorReflectiveCollection);

			return persistableFields;
		}

	}

}
