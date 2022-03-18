package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeHandlerCreator<D>
{
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerArray(Class<T> type) throws PersistenceExceptionTypeNotPersistable;
	
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerProxy(Class<T> type) throws PersistenceExceptionTypeNotPersistable;
	
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerLambda(Class<T> type) throws PersistenceExceptionTypeNotPersistable;
	
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerEnum(Class<T> type) throws PersistenceExceptionTypeNotPersistable;
	
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerEntity(Class<T> type) throws PersistenceExceptionTypeNotPersistable;
	
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerAbstract(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	public <T> PersistenceTypeHandler<D, T> createTypeHandlerUnpersistable(Class<T> type);

	public <T> PersistenceTypeHandler<D, T> createTypeHandlerGeneric(Class<T> type) throws PersistenceExceptionTypeNotPersistable;
	
	
	
	public abstract class Abstract<D> implements PersistenceTypeHandlerCreator<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeAnalyzer               typeAnalyzer              ;
		final PersistenceTypeResolver               typeResolver              ;
		final PersistenceFieldLengthResolver        lengthResolver            ;
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final PersistenceTypeAnalyzer               typeAnalyzer              ,
			final PersistenceTypeResolver               typeResolver              ,
			final PersistenceFieldLengthResolver        lengthResolver            ,
			final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator
		)
		{
			super();
			this.typeAnalyzer               = notNull(typeAnalyzer)              ;
			this.typeResolver               = notNull(typeResolver)              ;
			this.lengthResolver             = notNull(lengthResolver)            ;
			this.eagerStoringFieldEvaluator = notNull(eagerStoringFieldEvaluator);
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		public String deriveTypeName(final Class<?> type)
		{
			return this.typeResolver.deriveTypeName(type);
		}
		
		public PersistenceFieldLengthResolver lengthResolver()
		{
			return this.lengthResolver;
		}
		
		public PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator()
		{
			return this.eagerStoringFieldEvaluator;
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerArray(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			if(type.getComponentType().isPrimitive())
			{
				throw new PersistenceException(
					"Persisting primitive component type arrays requires a special-tailored "
					+ PersistenceTypeHandler.class.getSimpleName()
					+ " and cannot be done in a generic way."
				);
			}
			
			// array types can never change and therefore can never have obsolete types.
			return this.internalCreateTypeHandlerArray(type);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerProxy(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			throw new PersistenceException(
				"Proxy classes (subclasses of " + Proxy.class.getName() + ") are not supported."
			);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerLambda(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			throw new PersistenceException(
				"Lambdas are not supported as they cannot be resolved during loading"
				+ " due to insufficient reflection mechanisms provided by the (current) JVM."
			);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerEnum(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			return this.internalCreateTypeHandlerEnum(type);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerEntity(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			return this.internalCreateTypeHandlerEntity(type);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerAbstract(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			return this.internalCreateTypeHandlerAbstractType(type);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerUnpersistable(final Class<T> type)
		{
			return this.internalCreateTypeHandlerUnpersistable(type);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerGeneric(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			// collections need special handling to avoid dramatically inefficient generic structures
			if(XReflect.isJavaUtilCollectionType(type))
			{
				return this.internalCreateTypeHandlerJavaUtilCollection(type);
			}
			
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEntity(type, persistableFields, persisterFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.internalCreateTypeHandlerGeneric(type, persistableFields, persisterFields);
		}
		
		private static void checkNoProblematicFields(final Class<?> type, final XGettingEnum<Field> problematicFields)
		{
			if(problematicFields.isEmpty())
			{
				return;
			}
			
			throw new PersistenceException(
				"Type \"" + type.getName() +
				"\" not persistable due to problematic fields "
				+ problematicFields.toString()
			);
		}
		
		protected <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerEnum(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEnum(type, persistableFields, persisterFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.internalCreateTypeHandlerEnum(type, persistableFields, persisterFields);
		}
		
		protected <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerJavaUtilCollection(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsCollection(type, persistableFields, persisterFields, problematicFields);
			
			if(!problematicFields.isEmpty())
			{
				this.internalCreateTypeHandlerGenericJavaUtilCollection(type);
			}

			return this.internalCreateTypeHandlerGeneric(type, persistableFields, persisterFields);
		}
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerEnum(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields,
			XGettingEnum<Field> persisterFields
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerEntity(
			Class<T>            type
		);

		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerAbstractType(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerUnpersistable(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerArray(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerGeneric(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields,
			XGettingEnum<Field> persisterFields
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerGenericStateless(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerGenericJavaUtilCollection(
			Class<T> type
		);
		
	}
	
}
