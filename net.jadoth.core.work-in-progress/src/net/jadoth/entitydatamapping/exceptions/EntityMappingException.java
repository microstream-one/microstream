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
package net.jadoth.entitydatamapping.exceptions;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// TODO: Auto-generated Javadoc
/**
 * The Class EntityMappingException.
 * 
 * @author Thomas Muenz
 */
public class EntityMappingException extends RuntimeException
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2253201598939997276L;
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	/** The field. */
	private Field field = null;
	
	/** The data type. */
	private Class<?> dataType = null;
	
	/** The setter. */
	private Method setter = null;
	
	/** The getter. */
	private Method getter = null;

	/**
	 * Instantiates a new entity mapping exception.
	 */
	public EntityMappingException() {
	}

	/**
	 * Instantiates a new entity mapping exception.
	 * 
	 * @param message the message
	 */
	public EntityMappingException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new entity mapping exception.
	 * 
	 * @param cause the cause
	 */
	public EntityMappingException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new entity mapping exception.
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public EntityMappingException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////
	/**
	 * Gets the field.
	 * 
	 * @return the field
	 */
	public Field getField() {
		return this.field;
	}
	
	/**
	 * Gets the data type.
	 * 
	 * @return the dataType
	 */
	public Class<?> getDataType() {
		return this.dataType;
	}
	
	/**
	 * Gets the setter.
	 * 
	 * @return the setter
	 */
	public Method getSetter() {
		return this.setter;
	}
	
	/**
	 * Gets the getter.
	 * 
	 * @return the getter
	 */
	public Method getGetter() {
		return this.getter;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////
	/**
	 * Sets the field.
	 * 
	 * @param field the field to set
	 */
	public void setField(final Field field) {
		this.field = field;
	}
	
	/**
	 * Sets the data type.
	 * 
	 * @param dataType the dataType to set
	 */
	public void setDataType(final Class<?> dataType) {
		this.dataType = dataType;
	}
	
	/**
	 * Sets the setter.
	 * 
	 * @param setter the setter to set
	 */
	public void setSetter(final Method setter) {
		this.setter = setter;
	}
	
	/**
	 * Sets the getter.
	 * 
	 * @param getter the getter to set
	 */
	public void setGetter(final Method getter) {
		this.getter = getter;
	}
	
	
	/**
	 * @param s
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
	 */
	@Override
	public void printStackTrace(final PrintStream s) {
//		System.out.println("EntityMappingException.printStackTrace(PrintStream)");
		synchronized (s) {
			super.printStackTrace(s);
			s.print("Field:\t "); s.println(this.getField());
			s.print("Getter:\t "); s.println(this.getGetter());
			s.print("Setter:\t "); s.println(this.getSetter());
			s.print("Datatype:\t "); s.println(this.getDataType());
		}
	}

}
