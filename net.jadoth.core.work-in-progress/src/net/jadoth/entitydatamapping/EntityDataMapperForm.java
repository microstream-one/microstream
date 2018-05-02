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
import java.util.HashSet;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Interface EntityDataMapperForm.
 *
 * @param <E> the element type
 * @param <M> the generic type
 * @author Thomas Muenz
 */
public interface EntityDataMapperForm<E, M extends EntityDataMapper<E, ?, M>>
extends EntityDataMappingEnabled<E>
{

	/**
	 * Adds the mapper.
	 *
	 * @param mapper the mapper
	 * @return true, if successful
	 */
	public boolean addMapper(M mapper);

	/**
	 * Removes the mapper.
	 *
	 * @param mapper the mapper
	 * @return true, if successful
	 */
	public boolean removeMapper(M mapper);

	/**
	 * Clear.
	 */
	public void clear();

	/**
	 * Contains.
	 *
	 * @param mapper the mapper
	 * @return true, if successful
	 */
	public boolean contains(M mapper);

	/**
	 * Gets the mappers.
	 *
	 * @return the mappers
	 */
	public Set<M> getMappers();

	/**
	 * Sets the mappers.
	 *
	 * @param mapper the new mappers
	 */
	public void setMappers(Set<M> mapper);




	/**
	 * The Class Body.
	 *
	 * @param <E> the element type
	 * @param <M> the generic type
	 */
	public class Implementation<E, M extends EntityDataMapper<E, ?, M>>
	extends EntityDataMappingEnabled.AbstractImplementation<E>
	implements EntityDataMapperForm<E, M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		/** The mappers. */
		protected Set<EntityDataMapper<E, ?, M>> mappers = new HashSet<>();



		/**
		 * Instantiates a new body.
		 *
		 * @param entityClass the entity class
		 */
		public Implementation(final Class<E> entityClass) {
			super(entityClass);
		}

		/**
		 * Instantiates a new body.
		 *
		 * @param entity the entity
		 */
		public Implementation(final E entity) {
			super(entity);
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		/**
		 * @param mapper
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapperForm#addMapper(net.jadoth.entitydatamapping.EntityDataMapper)
		 */
		@Override
		public boolean addMapper(final M mapper) {
			mapper.setDataEntity(this.entity);
			return this.mappers.add(mapper);
		}

		/**
		 *
		 * @see net.jadoth.entitydatamapping.EntityDataMapperForm#clear()
		 */
		@Override
		public void clear() {
			this.mappers.clear();
		}

		/**
		 * @param mapper
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapperForm#contains(net.jadoth.entitydatamapping.EntityDataMapper)
		 */
		@Override
		public boolean contains(final M mapper) {
			return this.mappers.contains(mapper);
		}

		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapperForm#getMappers()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Set<M> getMappers() {
			return (Set<M>)this.mappers;
		}

		/**
		 * @param mapper
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMapperForm#removeMapper(net.jadoth.entitydatamapping.EntityDataMapper)
		 */
		@Override
		public boolean removeMapper(final M mapper) {
			return this.mappers.remove(mapper);
		}

		/**
		 * @param mappers
		 * @see net.jadoth.entitydatamapping.EntityDataMapperForm#setMappers(java.util.Set)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void setMappers(final Set<M> mappers)
		{
			final boolean relink = this.mappers != mappers;
			this.mappers = (Set<EntityDataMapper<E, ?, M>>) mappers;
			if(relink){
				this.linkEntityToAll();
			}
		}

		/**
		 * Sets the data entity.
		 *
		 * @param entity the entity
		 * @param forceRelink the force relink
		 * @return the entity data mapper form. body
		 */
		public EntityDataMapperForm.Implementation<E, M> setDataEntity(final E entity, final boolean forceRelink) {
			if(this.entity == null && entity == null) return this;

			final boolean relink = forceRelink || this.entity == null || entity == null?true
								 : this.entity != entity;
			super.setDataEntity(entity);
			if(relink){
				this.linkEntityToAll();
			}
			return this;
		}

		/**
		 * @param entity
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled.AbstractImplementation#setDataEntity(java.lang.Object)
		 */
		@Override
		public EntityDataMapperForm.Implementation<E, M> setDataEntity(final E entity) {
			return setDataEntity(entity, false);
		}

		/**
		 *
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#readFromEntity()
		 */
		@Override
		public void readFromEntity()
		{
			if(this.mappers == null)
			{
				return;
			}
			
			for(final EntityDataMapper<E, ?, M> c : this.mappers)
			{
				c.readFromEntity();
			}
		}

		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#saveToEntity()
		 */
		@Override
		public boolean saveToEntity()
		{
			if(this.isIncomplete())
			{
				return false;
			}

			if(!this.validateForSave())
			{
				return false;
			}

			for(final EntityDataMapper<E, ?, M> c : this.mappers)
			{
				//already validated beforehand, so don't do it again.
				c.saveToEntity(false);
			}
			return true;
		}

		/**
		 * @return
		 * @see net.jadoth.entitydatamapping.EntityDataMappingEnabled#validateForSave()
		 */
		@Override
		public boolean validateForSave()
		{
			for(final EntityDataMapper<E, ?, M> c : this.mappers)
			{
				if(!c.validateForSave()) return false;
			}
			return true;
		}





		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		/**
		 * Checks if is incomplete.
		 *
		 * @return true, if is incomplete
		 */
		protected boolean isIncomplete()
		{
			return this.entity == null || this.mappers == null;
		}

		/**
		 * Link entity to all.
		 */
		protected void linkEntityToAll()
		{
			if(this.isIncomplete())
			{
				return;
			}

			for(final EntityDataMapper<E, ?, M> c : this.mappers) {
				c.setDataEntity(this.entity);
			}
		}

	}
}
