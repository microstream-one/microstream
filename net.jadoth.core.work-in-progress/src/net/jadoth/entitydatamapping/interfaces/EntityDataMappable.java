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
package net.jadoth.entitydatamapping.interfaces;

import net.jadoth.entitydatamapping.EntityDataMapper;

// TODO: Auto-generated Javadoc
/**
 * The Interface EntityDataMappable.
 * 
 * @param <E> the element type
 * @param <D> the generic type
 * @param <M> the generic type
 * @author Thomas Muenz
 */
public interface EntityDataMappable<E, D, M extends EntityDataMapper<E,D,M>>
{
	
	/**
	 * Gets the mapper.
	 * 
	 * @return the mapper
	 */
	public EntityDataMapper<E, D, M> getMapper();
	
	/**
	 * Gets the data entity class.
	 * 
	 * @return the data entity class
	 */
	public Class<E> getDataEntityClass();
	
	/**
	 * Sets the data entity class.
	 * 
	 * @param dataEntityClass the new data entity class
	 */
	public void setDataEntityClass(Class<E> dataEntityClass);
	
	/**
	 * Sets the data type.
	 * 
	 * @param dataType the new data type
	 */
	public void setDataType(Class<D> dataType);
	
	/**
	 * Gets the data type.
	 * 
	 * @return the data type
	 */
	public Class<D> getDataType();
	
	/**
	 * Creates the mapper.
	 */
	public void createMapper();	

}
