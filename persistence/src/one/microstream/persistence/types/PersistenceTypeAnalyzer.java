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
	public XGettingEnum<Field> collectPersistableFields(
		Class<?>                                               type             ,
		XPrependingEnum<PersistenceTypeDescriptionMemberField> fieldDescriptions
	);



	public final class Implementation implements PersistenceTypeAnalyzer
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		//////////////////

		public static final void collectPersistableInstanceFields(
			final XPrependingSequence<Field> collection   ,
			final Class<?>                   entityType   ,
			final PersistenceFieldEvaluator  isPersistable
		)
		{

			XReflect.collectTypedFields(collection, entityType, field ->
				{
					if(!XReflect.isInstanceField(field) || !isPersistable.isPersistable(entityType, field))
					{
						return false;
					}
//					typeManager.ensureTypeId(field.getType());
					return true;
				}
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceFieldEvaluator fieldSelector;
		private final PersistenceTypeEvaluator  isPersistable;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final PersistenceTypeEvaluator  isPersistable,
			final PersistenceFieldEvaluator fieldSelector
		)
		{
			super();
			this.isPersistable = notNull(isPersistable);
			this.fieldSelector = notNull(fieldSelector);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public XGettingEnum<Field> collectPersistableFields(
			final Class<?>                                               type             ,
			final XPrependingEnum<PersistenceTypeDescriptionMemberField> fieldDescriptions
		)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();

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
			if(XReflect.isAbstract(type))
			{
				return persistableFields; // handle abstract types as having no fields at all / stateless types.
			}

			if(!this.isPersistable.isPersistableType(type))
			{
				throw new PersistenceExceptionTypeNotPersistable(type);
			}

			collectPersistableInstanceFields(
				persistableFields ,
				type              ,
				this.fieldSelector
//				typeManager
			);

			return persistableFields;
		}

	}

}
