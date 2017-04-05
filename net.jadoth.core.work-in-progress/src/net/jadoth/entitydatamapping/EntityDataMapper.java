/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.entitydatamapping;
import static net.jadoth.reflect.JadothReflect.addAllFields;
import static net.jadoth.reflect.JadothReflect.addAllMethods;
import static net.jadoth.reflect.JadothReflect.getMembersByLabel;
import static net.jadoth.reflect.JadothReflect.getMembersWithAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import net.jadoth.entitydatamapping.exceptions.EntityDataInvalidAccessorMethodException;
import net.jadoth.entitydatamapping.exceptions.EntityGetDataException;
import net.jadoth.entitydatamapping.exceptions.EntitySetDataException;
import net.jadoth.exceptions.NoSuchFieldRuntimeException;
import net.jadoth.exceptions.NoSuchMethodRuntimeException;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.util.JadothTypes;


// TODO: Auto-generated Javadoc
/**
 * The Interface EntityDataMapper.
 *
 * @param <E> the element type
 * @param <D> the generic type
 * @param <M> the generic type
 * @author Thomas Muenz
 */
public interface EntityDataMapper<E, D, M extends EntityDataMapper<E, D, M>> extends EntityDataMappingEnabled<E>
{

	/**
	 * Searches for a @Label Annotation with the given label or a field or appropriate method with name like the given
	 * label.
	 *
	 * @param label the label
	 * @param useForAccessorMethods the use for accessor methods
	 * @return the m
	 * @see net.jadoth.reflect.Label
	 */
	public M assignDataAccessByLabel(String label, boolean useForAccessorMethods);

	/**
	 * <code>searchString</code> is used to search for a field or an appropriate getter or setter in the class of
	 * the set entity following these rules (in order):<br>
	 * - if a field has <code>searchString</code> as its name and has the right type,
	 * it is assigned as the data field.<br>
	 * <br>
	 * - if no field has been found and <code>searchForAccessorMethods</code> is <code>true</code>,
	 * a search for a fitting getter or setter with the name <code>searchString</code> is performed.<br>
	 * - if no field has been found, then field, getter and setter are tried to be found by searching for
	 * <code>searchString</code> as a label (Annotation @Label).
	 * <br>Getter and setter only if <code>searchForAccessorMethods</code> is <code>true</code> and they haven't
	 * already been found before.<br>
	 * <br>
	 * - if a field has finally been found and <code>searchForAccessorMethods</code> is <code>true</code>,
	 * then getters and setters are tried to be derived from that field IF they haven't been found before.<br>
	 * <br>
	 * Deriving getter and setter works in compliance to common pattern:<br>
	 * fieldname -> get[Fieldname]() or set[Fieldname](value).
	 *
	 * @param searchString the search string
	 * @param searchForAccessorMethods the search for accessor methods
	 * @return the m
	 * @see net.jadoth.reflect.Label
	 */
	public M assignDataAccessBySearchString(String searchString, boolean searchForAccessorMethods);

	/**
	 * Assign data access by annotation.
	 *
	 * @param dataFieldAnnotation the data field annotation
	 * @param searchForAccessorMethods if getters and setters should be searched for as well
	 * @return the m
	 */
	public M assignDataAccessByAnnotation(
		Class<? extends Annotation> dataFieldAnnotation,
		boolean searchForAccessorMethods
	);

	/**
	 * Assign data field.
	 *
	 * @param dataField the data field
	 * @param deriveAccessorMethods the derive accessor methods
	 * @return the m
	 */
	public M assignDataField(Field dataField, boolean deriveAccessorMethods);

	/**
	 * Assign data getter.
	 *
	 * @param getter the getter
	 * @return the m
	 * @throws EntityGetDataException the entity get data exception
	 */
	public M assignDataGetter(Method getter) throws EntityGetDataException;

	/**
	 * Assign data setter.
	 *
	 * @param setter the setter
	 * @return the m
	 * @throws EntitySetDataException the entity set data exception
	 */
	public M assignDataSetter(Method setter) throws EntitySetDataException;

	/**
	 * Sets the use accessor methods.
	 *
	 * @param useAccessorMethods the use accessor methods
	 * @return the m
	 */
	public M setUseAccessorMethods(boolean useAccessorMethods);

	/**
	 * Checks if is use accessor methods.
	 *
	 * @return true, if is use accessor methods
	 */
	public boolean isUseAccessorMethods();


	/**
	 * Gets the data type.
	 *
	 * @return the data type
	 */
	public Class<D> getDataType();

	/**
	 * Gets the entity data value.
	 *
	 * @return the entity data value
	 */
	public D getEntityDataValue();

	/**
	 * Sets the entity data value.
	 *
	 * @param value the value
	 * @return the m
	 */
	public M setEntityDataValue(D value);

	/**
	 * Save to entity.
	 *
	 * @param validate the validate
	 * @return true, if successful
	 */
	public boolean saveToEntity(boolean validate);

	/**
	 * The Class AbstractBody.
	 *
	 * @param <E> the element type
	 * @param <D> the generic type
	 * @param <M> the generic type
	 */
	public static abstract class AbstractImplementation<E, D, M extends AbstractImplementation<E, D, M>>
	extends EntityDataMappingEnabled.AbstractImplementation<E>
	implements EntityDataMapper<E, D, M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		/** The field. */
		protected Field field = null;

		/** The setter. */
		protected Method setter = null;

		/** The getter. */
		protected Method getter = null;

		/** The data annotation. */
		protected Class<? extends Annotation> dataAnnotation = null;

		/** The use accessor methods. */
		protected boolean useAccessorMethods = true;

		/** The data type. */
		protected Class<D> dataType = null;

		/** The cached methods. */
		protected HashSet<Method> cachedMethods = null;

		/** The cached fields. */
		protected HashSet<Field> cachedFields = null;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		/**
		 * Instantiates a new abstract body.
		 *
		 * @param entityClass the entity class
		 * @param dataType the data type
		 */
		public AbstractImplementation(final Class<E> entityClass, final Class<D> dataType){
			super(entityClass);
			this.dataType = dataType;
			if(entityClass != null){
				this.cacheMembers();
			}
		}

		/**
		 * Instantiates a new abstract body.
		 *
		 * @param entity the entity
		 * @param dataType the data type
		 */
		@SuppressWarnings("unchecked")
		public AbstractImplementation(final E entity, final Class<D> dataType){
			this((Class<E>)entity.getClass(), dataType);
			this.dataType = dataType;
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////
		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#isUseAccessorMethods()
		 */
		@Override
		public boolean isUseAccessorMethods() {
			return this.useAccessorMethods;
		}

		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#getDataType()
		 */
		@Override
		public Class<D> getDataType()
		{
			return this.dataType;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////
		/**
		 * @param useAccessorMethods
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#setUseAccessorMethods(boolean)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M setUseAccessorMethods(final boolean useAccessorMethods)
		{
			this.useAccessorMethods = useAccessorMethods;
			return (M)this;
		}


		/**
		 * Fill entity get data exception.
		 *
		 * @param e the e
		 * @return the entity get data exception
		 */
		protected EntityGetDataException fillEntityGetDataException(final EntityGetDataException e)
		{
			e.setField(this.field);
			e.setGetter(this.getter);
			e.setDataType(this.dataType);
			return e;
		}

		/**
		 * Creates the read exception.
		 *
		 * @param message the message
		 * @return the entity get data exception
		 */
		protected EntityGetDataException createReadException(final String message)
		{
			return fillEntityGetDataException(new EntityGetDataException(message));

		}

		/**
		 * Creates the read exception.
		 *
		 * @param cause the cause
		 * @return the entity get data exception
		 */
		protected EntityGetDataException createReadException(final Throwable cause){
			return fillEntityGetDataException(new EntityGetDataException(cause));
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		/**
		 * @throws EntityGetDataException
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#readFromEntity()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void readFromEntity() throws EntityGetDataException
		{
			try{
				// (09.09.2009 TM)NOTE: safety of the assignment is insured by getter type validation!
				D dataValue = null;

				if(this.getter != null && this.useAccessorMethods){
					dataValue = (D)this.getter.invoke(this.getDataEntity());
				}
				else if(this.field != null){
					dataValue = (D)this.field.get(this.getDataEntity());
				}
				else {
					final EntityGetDataException e = createReadException("No entity data access found.");
					e.fillInStackTrace();
					throw e;
				}
				this.setEntityDataValue(dataValue);
			}
			catch(final Exception e){
				final EntityGetDataException e2 = createReadException(e);
				e2.fillInStackTrace();
				throw e2;
			}
		}

		/**
		 * @return
		 * @throws EntitySetDataException
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#saveToEntity()
		 */
		@Override
		public boolean saveToEntity() throws EntitySetDataException
		{
			return this.saveToEntity(true);
		}

		/**
		 * @param validate
		 * @return
		 * @throws EntitySetDataException
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#saveToEntity(boolean)
		 */
		@Override
		public boolean saveToEntity(final boolean validate) throws EntitySetDataException
		{
			if(validate && !this.validateForSave()) return false;

			final E dataEntity = this.getDataEntity();
			final D entityDataValue = this.getEntityDataValue();

			// (09.09.2009 TM)NOTE: safety of the assignments is insured by setter type validation!
			if(this.setter != null && this.useAccessorMethods){
				try{
					this.setter.invoke(dataEntity, entityDataValue);
				}
				catch(final Exception e){
					throw new EntitySetDataException(
						"Setter: "+this.setter+"\n"+
						"DataEntity: "+dataEntity+"\n"+
						"EntityDataValue: "+entityDataValue+"\n"
					,e);
				}
			}
			else if(this.field != null){
				try{
					this.field.set(dataEntity, entityDataValue);
				}
				catch(final Exception e){
					throw new EntitySetDataException(
						"Field: "+this.field+"\n"+
						"DataEntity: "+dataEntity+"\n"+
						"EntityDataValue: "+entityDataValue+"\n"
					,e);
				}
			}
			else {
				throw new EntitySetDataException("No entity data access given");
			}
			// (29.05.2010)TODO multi-exception-catching with Java 7

			return true;
		}

		/**
		 * @param dataField
		 * @param deriveAccessorMethods
		 * @return
		 * @throws NoSuchFieldRuntimeException
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#assignDataField(java.lang.reflect.Field, boolean)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M assignDataField(final Field dataField, final boolean deriveAccessorMethods) throws NoSuchFieldRuntimeException
		{
			if(this.cachedFields != null && !lookupFieldInEntityClass(dataField)){
				//cachedFields==null: allow "on spec" use of the Field, back-check when caching members.
				throw new NoSuchFieldRuntimeException(new NoSuchFieldException(dataField.getName()));
			}
			this.field = dataField;

			if(deriveAccessorMethods){
				this.deriveAccessorMethodsFromField(dataField, deriveAccessorMethods, deriveAccessorMethods);
			}
			return (M)this;
		}

		/**
		 * @param getter
		 * @return
		 * @throws EntityDataInvalidAccessorMethodException
		 * @throws NoSuchMethodRuntimeException
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#assignDataGetter(java.lang.reflect.Method)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M assignDataGetter(final Method getter)
			throws EntityDataInvalidAccessorMethodException, NoSuchMethodRuntimeException
		{
			if(this.cachedMethods != null && !lookupMethodInEntityClass(getter)){
				//cachedMethods==null: allow "on spec" use of the Method, back-check when caching methods.
				throw new NoSuchMethodRuntimeException(new NoSuchMethodException(getter.getName()));
			}
			if(!validateGetter(getter)){
				throw new NoSuchMethodRuntimeException(new NoSuchMethodException(getter.getName()));
			}

			this.getter = getter;
			return (M)this;
		}

		/**
		 * @param setter
		 * @return
		 * @throws EntityDataInvalidAccessorMethodException
		 * @throws NoSuchMethodRuntimeException
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#assignDataSetter(java.lang.reflect.Method)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M assignDataSetter(final Method setter)
			throws EntityDataInvalidAccessorMethodException, NoSuchMethodRuntimeException
		{
			if(this.cachedMethods != null && !lookupMethodInEntityClass(setter)){
				//cachedMethods==null: allow "on spec" use of the Method, back-check when caching methods.
				throw new NoSuchMethodRuntimeException(new NoSuchMethodException(setter.getName()));
			}
			if(!validateGetter(setter)){
				throw new EntityDataInvalidAccessorMethodException(setter);
			}
			this.setter = setter;
			return (M)this;
		}

		/**
		 * @param annotation
		 * @param useForAccessorMethods
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#assignDataAccessByAnnotation(java.lang.Class, boolean)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M assignDataAccessByAnnotation(final Class<? extends Annotation> annotation, final boolean useForAccessorMethods)
		{
			this.dataAnnotation = annotation;
			this.searchForFieldByAnnotation();
			if(useForAccessorMethods){
				this.searchForAccessMethodsByAnnotation(true, true);
			}
			return (M)this;
		}

		/**
		 * @param label
		 * @param searchForAccessorMethods
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#assignDataAccessByLabel(java.lang.String, boolean)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M assignDataAccessByLabel(final String label, final boolean searchForAccessorMethods)
		{
			assignDataAccessByLabel(label, searchForAccessorMethods, searchForAccessorMethods);
			return (M)this;
		}

		/**
		 * @param searchString
		 * @param searchForAccessorMethods
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapper#assignDataAccessBySearchString(java.lang.String, boolean)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public M assignDataAccessBySearchString(final String searchString, boolean searchForAccessorMethods)
		{
			/* (16.09.2009 TM)NOTE:
			 * Accessor method search is completely independent from useAccessorMethods.
			 * Because useAccessorMethods could be switched on later, so the methods must be present.
			 */
			searchForAccessorMethods &= this.cachedMethods != null;

			boolean searchGetter = searchForAccessorMethods;
			boolean searchSetter = searchForAccessorMethods;
			final Field currentField = this.field;
			final Method oldGetter = this.getter;
			final Method oldSetter = this.setter;

			Field foundField = null;

			if(this.cachedFields != null){
				//1.) Search for field by name
				foundField = getCachedDataField(searchString);
			}

			//1.a) field not found
			if(foundField == null){
				//1.a.1) No field found: search for accessor methods by name
				if(searchForAccessorMethods){
					final Method getter = getCachedGetter(searchString);
					if(getter != null){
						this.getter = getter;
						searchGetter = false;
					}
					final Method setter = getCachedSetter(searchString);
					if(setter != null){
						this.setter = setter;
						searchSetter = false;
					}
				}
				//1.a.2) Search by label for field and for accessor methods that haven't been found by name
				assignDataAccessByLabel(searchString, searchGetter, searchSetter);
			}
			else {
				this.field = foundField;
			}


			//1.b) If new field has been found check if accessor methods should be derived from it
			if(this.field != null && this.field != currentField){
				searchGetter = searchForAccessorMethods && (this.getter == null || this.getter == oldGetter);
				searchSetter = searchForAccessorMethods && (this.setter == null || this.setter == oldSetter);
				this.deriveAccessorMethodsFromField(foundField, searchGetter, searchSetter);
			}
			return (M)this;
		}





		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		/**
		 * Cache members.
		 */
		protected void cacheMembers()
		{
			if(this.entityClass == null){
				this.cachedMethods = null;
				this.cachedFields = null;
				this.field = null;
				this.getter = null;
				this.setter = null;
			}
			else {
				this.cachedMethods = addAllMethods(this.entityClass, 0, new HashSet<Method>(50));
				this.cachedFields = addAllFields(this.entityClass, 0, new HashSet<Field>(50));

				//if newly cached fields do not contain current field then remove the field
				if(this.field != null && !this.cachedFields.contains(this.field)){
					this.field = null;
					//search for field by annotation if one is present
					this.searchForFieldByAnnotation();
				}
				//if newly cached methods do not contain current getter then remove the getter
				if(this.getter != null && !this.cachedMethods.contains(this.getter)){
					this.getter = null;
				}
				//if newly cached methods do not contain current setter then remove the setter
				if(this.setter != null && !this.cachedMethods.contains(this.setter)){
					this.setter = null;
				}
				this.searchForAccessMethodsByAnnotation(this.getter == null, this.setter == null);
				this.deriveAccessorMethodsFromField(this.field, this.getter == null, this.setter == null);
			}
		}

		/**
		 * Lookup field in entity class.
		 *
		 * @param f the f
		 * @return true, if successful
		 */
		protected boolean lookupFieldInEntityClass(final Field f)
		{
			if(this.cachedFields == null)
			{
				return false;
			}
			
			return this.cachedFields.contains(f);
		}

		/**
		 * Lookup method in entity class.
		 *
		 * @param m the m
		 * @return true, if successful
		 */
		protected boolean lookupMethodInEntityClass(final Method m)
		{
			if(this.cachedMethods == null)
			{
				return false;
			}
			
			return this.cachedMethods.contains(m);
		}

		/**
		 * Validate setter.
		 *
		 * @param setter the setter
		 * @return true, if successful
		 */
		protected boolean validateSetter(final Method setter)
		{
			return setter == null
				? false
				: JadothReflect.validateSetter(setter, this.dataType, false)
			;
		}

		/**
		 * Validate getter.
		 *
		 * @param getter the getter
		 * @return true, if successful
		 */
		protected boolean validateGetter(final Method getter){
			return getter == null ?false
					:JadothReflect.validateGetter(getter, this.dataType);
		}

		/**
		 * Derive accessor methods from field.
		 *
		 * @param field the field
		 * @param searchGetter the search getter
		 * @param searchSetter the search setter
		 */
		protected void deriveAccessorMethodsFromField(final Field field, final boolean searchGetter, final boolean searchSetter)
		{
			if(field == null || this.cachedMethods == null) return;

			if(searchGetter){
				String getterName = JadothReflect.deriveGetterNameFromField(field);
				Method getter = getCachedGetter(getterName);
				if(getter != null){
					this.getter = getter;
				}
				else {
					// (15.12.2009 TM)NOTE: Fix: if type is boolean, try again without "is" special case (getXXX)
					booleanSpecialCase: {
						if(!JadothTypes.isBoolean(field.getType())) break booleanSpecialCase;

						getterName = JadothReflect.deriveGetterNameFromField(field, false);
						getter = getCachedGetter(getterName);
						if(getter != null){
							this.getter = getter;
						}
					}
				}
			}

			if(searchSetter){
				final String setterName = JadothReflect.deriveSetterNameFromField(field);
				final Method setter = getCachedSetter(setterName);
				if(setter != null){
					this.setter = setter;
				}
				// (15.12.2009 TM)NOTE: No B/boolean special case treatment needed for setter
			}
		}

		/**
		 * Gets the cached setter.
		 *
		 * @param setterName the setter name
		 * @return the cached setter
		 */
		protected Method getCachedSetter(final String setterName)
		{
			final Method setter = getCachedMethod(setterName, this.dataType);
			if(validateSetter(setter)){
				return setter;
			}
			return null;
		}

		/**
		 * Gets the cached getter.
		 *
		 * @param getterName the getter name
		 * @return the cached getter
		 */
		protected Method getCachedGetter(final String getterName)
		{
			final Method getter = getCachedMethod(getterName);
			if(validateGetter(getter)){
				return getter;
			}
			return null;
		}

		/**
		 * Gets the cached method.
		 *
		 * @param methodName the method name
		 * @param parameterTypes the parameter types
		 * @return the cached method
		 */
		protected Method getCachedMethod(final String methodName, Class<?>... parameterTypes)
		{
			if(this.cachedMethods == null) return null;

			if(parameterTypes == null){
				parameterTypes = new Class<?>[0];
			}

			for(final Method m : this.cachedMethods) {
				if(m.getName().equals(methodName) && Arrays.equals(m.getParameterTypes(), parameterTypes)){
					return m;
				}
			}
			return null;
		}

		/**
		 * Gets the cached field.
		 *
		 * @param fieldName the field name
		 * @return the cached field
		 */
		protected Field getCachedField(final String fieldName)
		{
			if(this.cachedFields == null){
				return null;
			}
			for(final Field f : this.cachedFields) {
				if(f.getName().equals(fieldName)){
					return f;
				}
			}
			return null;
		}

		/**
		 * Gets the cached data field.
		 *
		 * @param fieldName the field name
		 * @return the cached data field
		 */
		protected Field getCachedDataField(final String fieldName)
		{
			final Field f = this.getCachedField(fieldName);
			if(f != null && f.getType() == this.dataType){
				return f;
			}
			return null;
		}

		/**
		 * Search for field by annotation.
		 */
		protected void searchForFieldByAnnotation()
		{
			if(this.cachedFields != null && this.dataAnnotation != null){
				final Field[] cachedFieldArray = this.cachedFields.toArray(new Field[this.cachedFields.size()]);
				final Field[] annotatedFields = getMembersWithAnnotation(this.dataAnnotation, cachedFieldArray);
				this.setAppropriateDataField(annotatedFields);
			}
		}

		/**
		 * Search for access methods by annotation.
		 *
		 * @param searchGetter the search getter
		 * @param searchSetter the search setter
		 */
		protected void searchForAccessMethodsByAnnotation(final boolean searchGetter, final boolean searchSetter)
		{
			if(this.cachedMethods == null || this.dataAnnotation == null) return;

			final Method[] cachedMethodsArray = this.cachedMethods.toArray(new Method[this.cachedMethods.size()]);
			final Method[] annotatedMethods = getMembersWithAnnotation(this.dataAnnotation, cachedMethodsArray);
			this.setAppropriateAccessMethods(annotatedMethods, searchGetter, searchSetter);

		}

		/**
		 * Sets the appropriate access methods.
		 *
		 * @param methods the methods
		 * @param searchGetter the search getter
		 * @param searchSetter the search setter
		 */
		protected void setAppropriateAccessMethods(final Method[] methods, boolean searchGetter, boolean searchSetter)
		{
			for(final Method m : methods) {
				if(searchGetter){
					if(this.validateGetter(m)){
						this.getter = m;
						searchGetter = false;
					}
				}
				else if(searchSetter){
					if(this.validateSetter(m)){
						this.setter = m;
						searchSetter = false;
					}
				}
				else {
					//if both have been found there's no need to continue searching.
					break;
				}
			}
		}

		/**
		 * Sets the appropriate data field.
		 *
		 * @param fields the fields
		 * @return true, if successful
		 */
		protected boolean setAppropriateDataField(final Field[] fields){
			for(final Field f : fields) {
				if(f.getType() == this.dataType){
					this.field = f;
					return true;
				}
			}
			return false;
		}

		/**
		 * Assign data access by label.
		 *
		 * @param label the label
		 * @param searchGetter the search getter
		 * @param searchSetter the search setter
		 */
		protected void assignDataAccessByLabel(final String label, boolean searchGetter, boolean searchSetter)
		{
			final Field currentField = this.field;
			final Method oldGetter = this.getter;
			final Method oldSetter = this.setter;

			if(this.cachedFields != null){
				final Field[] cachedFieldArray = this.cachedFields.toArray(new Field[this.cachedFields.size()]);
				final Field[] labeledFields = getMembersByLabel(label, cachedFieldArray);
				this.setAppropriateDataField(labeledFields);
			}

			if(!searchGetter && !searchSetter) return;

			if(this.cachedMethods != null){
				final Method[] cachedMethodArray = this.cachedMethods.toArray(new Method[this.cachedMethods.size()]);
				final Method[] labeledMethods = getMembersByLabel(label, cachedMethodArray);
				this.setAppropriateAccessMethods(labeledMethods, searchGetter, searchSetter);
			}

			if(this.field != null && this.field != currentField){
				searchGetter &= oldGetter == this.getter;
				searchSetter &= oldSetter == this.setter;
				this.deriveAccessorMethodsFromField(this.field, searchGetter, searchSetter);
			}
		}




		//Default implementation, does no validation at all.
		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#validateForSave()
		 */
		@Override
		public boolean validateForSave() {
			return true;
		}



		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final char n = '\n';
			final char t = '\t';
			final StringBuilder sb = new StringBuilder(1024);
			sb.append(super.toString()).append(n)
			.append("DataEntity:").append(t).append(this.entity.toString().replaceAll("\\n", " ")).append(n)
			.append("DataType:").append(t).append(this.dataType).append(n)
			.append("DataField:").append(t).append(this.field).append(n)

			.append(" ").append("useMethods:").append(t).append(this.useAccessorMethods).append(n)
			.append(" ").append("DataGetter:").append(t).append(this.getter).append(n)
			.append(" ").append("DataSetter:").append(t).append(this.setter).append(n)

			.append("Annotation:").append(t).append(this.dataAnnotation).append(n)
			;
			return sb.toString();
		}







	}





}
