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

// TODO: Auto-generated Javadoc
/**
 * The Interface EntityDataMappingEnabled.
 * 
 * @param <E> the element type
 * @author Thomas Muenz
 */
public interface EntityDataMappingEnabled<E>
{
	
	/**
	 * Sets the data entity.
	 * 
	 * @param entity the entity
	 * @return the entity data mapping enabled
	 */
	public EntityDataMappingEnabled<E> setDataEntity(E entity);
	
	/**
	 * Gets the data entity.
	 * 
	 * @return the data entity
	 */
	public E getDataEntity();
	
	/**
	 * Save to entity.
	 * 
	 * @return true, if successful
	 */
	public boolean saveToEntity();
	
	/**
	 * Validate for save.
	 * 
	 * @return true, if successful
	 */
	public boolean validateForSave();
	
	/**
	 * Read from entity.
	 */
	public void readFromEntity();
	
	/**
	 * Gets the data entity class.
	 * 
	 * @return the data entity class
	 */
	public Class<E> getDataEntityClass();

	
	/**
	 * The Class AbstractBody.
	 * 
	 * @param <E> the element type
	 */
	public static abstract class AbstractImplementation<E> implements EntityDataMappingEnabled<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		/** The entity class. */
		protected final Class<E> entityClass;
		
		/** The entity. */
		protected E entity = null;
		

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		/**
		 * Instantiates a new abstract body.
		 * 
		 * @param entityClass the entity class
		 */
		public AbstractImplementation(final Class<E> entityClass) {
			super();
			this.entityClass = entityClass;
		}
		
		/**
		 * Instantiates a new abstract body.
		 * 
		 * @param entity the entity
		 */
		@SuppressWarnings("unchecked")
		public AbstractImplementation(final E entity) {
			this((Class<E>)(entity==null?null:entity.getClass()));
			this.entity = entity;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////
		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#getDataEntity()
		 */
		@Override
		public E getDataEntity()
		{
			return this.entity;
		}
		
		/**
		 * Gets the data entity class.
		 * 
		 * @return the entityClass
		 */
		@Override
		public Class<E> getDataEntityClass()
		{
			return this.entityClass;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////
		/**
		 * @param entity
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#setDataEntity(java.lang.Object)
		 */
		@Override
		public EntityDataMappingEnabled.AbstractImplementation<E> setDataEntity(final E entity)
		{
			if(entity != null && !this.entityClass.isInstance(entity))
			{
				throw new ClassCastException(entity.getClass().toString());
			}
			this.entity = entity;
			return this;
		}
		
	}
	
}
