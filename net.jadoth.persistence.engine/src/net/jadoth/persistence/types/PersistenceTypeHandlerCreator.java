package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

public interface PersistenceTypeHandlerCreator<M>
{
	public <T> PersistenceTypeHandler<M, T> createTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	
	
	public abstract class AbstractImplementation<M> implements PersistenceTypeHandlerCreator<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeAnalyzer               typeAnalyzer           ;
		final PersistenceFieldLengthResolver        lengthResolver         ;
		final PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractImplementation(
			final PersistenceTypeAnalyzer               typeAnalyzer           ,
			final PersistenceFieldLengthResolver        lengthResolver         ,
			final PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator
		)
		{
			super();
			this.typeAnalyzer            = notNull(typeAnalyzer)           ;
			this.lengthResolver          = notNull(lengthResolver)         ;
			this.mandatoryFieldEvaluator = notNull(mandatoryFieldEvaluator);
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public PersistenceFieldLengthResolver lengthResolver()
		{
			return this.lengthResolver;
		}
		
		public PersistenceEagerStoringFieldEvaluator mandatoryFieldEvaluator()
		{
			return this.mandatoryFieldEvaluator;
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> createTypeHandler(final Class<T> type)
		{
			// should never happen or more precisely: should only happen for unhandled primitives
			if(type.isPrimitive())
			{
				// (29.04.2017 TM)EXCP: proper exception
				throw new RuntimeException("Primitive types must be handled by default (dummy) handler implementations.");
			}
			
			// array special casing
			if(type.isArray())
			{
				// array special cases
				if(type.getComponentType().isPrimitive())
				{
					// (01.04.2013)EXCP: proper exception
					throw new RuntimeException("Primitive component type arrays must be handled by default handler implementations.");
				}
				
				// array types can never change and therefore can never have obsolete types.
				return this.createTypeHandlerArray(type);
			}

			// create generic handler for all other cases ("normal" classes without predefined handler)
			return this.createGenericHandler(type);
		}
		
		protected <T> PersistenceTypeHandler<M, T> createGenericHandler(final Class<T> type)
		{
			final HashEnum<PersistenceTypeDescriptionMemberField> fieldDescriptions = HashEnum.New();

			final XGettingEnum<Field> persistableFields =
				this.typeAnalyzer.collectPersistableFields(type, fieldDescriptions)
			;
			
			return this.createTypeHandlerReflective(type, persistableFields);
		}
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerArray(Class<T> type);
		
		protected abstract <T> PersistenceTypeHandler<M, T> createTypeHandlerReflective(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields
		);
		
	}
	
}
